package com.dkbsign.signature;

import com.dkbsign.DkbSignClient;
import com.dkbsign.enums.SignatureLevel;
import com.dkbsign.enums.SignatureType;
import com.dkbsign.model.SignatureMark;
import com.dkbsign.response.OtpResponse;
import com.dkbsign.response.SecondFactorResponse;
import com.dkbsign.response.SelfSignBatchResponse;
import com.dkbsign.response.SelfSignResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelfSignServiceTest {

    private static final Pattern PARAMS_PART =
            Pattern.compile("name=\"params\"\\r\\n.*?\\r\\n\\r\\n(\\{.*?})\\r\\n--", Pattern.DOTALL);

    @TempDir
    Path dir;

    private MockWebServer server;
    private SelfSignService selfSign;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        selfSign = DkbSignClient.builder()
                .baseUrl(server.url("/").toString())
                .token("tok")
                .build()
                .selfSign();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void sendOtp() throws Exception {
        server.enqueue(json(200, "{\"message\":\"OTP sent\",\"dev_otp_code\":\"482913\"}"));

        OtpResponse response = selfSign.sendOtp();

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v4/sign/otp", request.getPath());
        assertEquals("Bearer tok", request.getHeader("Authorization"));
        assertEquals("application/json", request.getHeader("Accept"));
        assertEquals("482913", response.getDevOtpCode());
    }

    @Test
    void checkSecondFactorSendsLevel() throws Exception {
        server.enqueue(json(200, "{\"required\":true}"));

        SecondFactorResponse response = selfSign.checkSecondFactor(SignatureLevel.QUALIFIED);

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/v4/sign/second-factor?level=qualified", request.getPath());
        assertTrue(response.isRequired());
    }

    @Test
    void sendSecondOtp() throws Exception {
        server.enqueue(json(200, "{\"message\":\"sent\"}"));

        selfSign.sendSecondOtp();

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/v4/sign/second-otp", request.getPath());
    }

    @Test
    void signSendsMultipartWithParams() throws Exception {
        server.enqueue(json(200, "{\"message\":\"ok\",\"document_uuid\":\"u-1\","
                + "\"signed_pdf_url\":\"https://x/download\",\"verification_url\":\"https://x/verify\"}"));

        SelfSignResponse response = selfSign.sign(SelfSignRequest.builder()
                .file(file("doc.pdf"))
                .signatureImage(file("sig.png"))
                .level(SignatureLevel.QUALIFIED)
                .mark(0, SignatureMark.at(120, 200).size(200, 70).type(SignatureType.SIGNATURE))
                .mark(SignatureMark.anchor("{{signature}}"))
                .otpCode("111111")
                .mfaCode("222222")
                .build());

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readString(StandardCharsets.ISO_8859_1);
        assertEquals("/api/v4/sign", request.getPath());
        assertTrue(request.getHeader("Content-Type").startsWith("multipart/form-data"));
        assertTrue(body.contains("name=\"file\"; filename=\"doc.pdf\""));
        assertTrue(body.contains("Content-Type: application/pdf"));
        assertTrue(body.contains("name=\"signature_image\"; filename=\"sig.png\""));
        assertEquals(
                "{\"pages\":["
                        + "{\"page\":0,\"signatures\":[{\"x\":120.0,\"y\":200.0,\"width\":200.0,\"height\":70.0,\"type\":\"signature\"}]},"
                        + "{\"signatures\":[{\"anchor\":\"{{signature}}\"}]}],"
                        + "\"signature_level\":\"qualified\",\"otp_code\":\"111111\",\"mfa_code\":\"222222\"}",
                params(body));

        assertEquals("u-1", response.getDocumentUuid());
        assertEquals("https://x/download", response.getSignedPdfUrl());
        assertEquals("https://x/verify", response.getVerificationUrl());
    }

    @Test
    void signOmitsOptionalParts() throws Exception {
        server.enqueue(json(200, "{\"message\":\"ok\"}"));

        selfSign.sign(SelfSignRequest.builder()
                .file(file("doc.pdf"))
                .mark(0, SignatureMark.at(10, 10))
                .build());

        String body = server.takeRequest().getBody().readString(StandardCharsets.ISO_8859_1);
        assertFalse(body.contains("signature_image"));
        assertEquals("{\"pages\":[{\"page\":0,\"signatures\":[{\"x\":10.0,\"y\":10.0}]}]}", params(body));
    }

    @Test
    void signRejectsSeveralFiles() throws IOException {
        SelfSignRequest request = SelfSignRequest.builder()
                .addFile(file("a.pdf"))
                .addFile(file("b.pdf"))
                .mark(0, SignatureMark.at(10, 10))
                .build();

        assertThrows(IllegalArgumentException.class, () -> selfSign.sign(request));
        assertEquals(0, server.getRequestCount());
    }

    @Test
    void signAllSendsFilesArrayAndReportsPartialSuccess() throws Exception {
        server.enqueue(json(207, "{\"message\":\"Partial\",\"results\":[{},{}]}"));

        SelfSignBatchResponse response = selfSign.signAll(SelfSignRequest.builder()
                .addFile(file("a.pdf"))
                .addFile(file("b.pdf"))
                .mark(0, SignatureMark.at(10, 10))
                .build());

        String body = server.takeRequest().getBody().readString(StandardCharsets.ISO_8859_1);
        assertTrue(body.contains("name=\"files[]\"; filename=\"a.pdf\""));
        assertTrue(body.contains("name=\"files[]\"; filename=\"b.pdf\""));
        assertFalse(body.contains("name=\"file\";"));

        assertEquals(207, response.getStatusCode());
        assertTrue(response.isPartial());
        assertEquals("Partial", response.getMessage());
        assertEquals(2, response.getBody().get("results").size());
    }

    private static MockResponse json(int status, String body) {
        return new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private static String params(String multipartBody) {
        Matcher matcher = PARAMS_PART.matcher(multipartBody);
        assertTrue(matcher.find(), "params part not found");
        return matcher.group(1);
    }

    private Path file(String name) throws IOException {
        return Files.writeString(dir.resolve(name), "content");
    }
}
