package com.dkbsign.publicenvelope;

import com.dkbsign.DkbSignClient;
import com.dkbsign.enums.SignatureType;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicEnvelopeServiceTest {

    private okhttp3.mockwebserver.MockWebServer server;
    private PublicEnvelopeService session;

    @BeforeEach
    void setUp() throws IOException {
        server = new okhttp3.mockwebserver.MockWebServer();
        server.start();
        session = DkbSignClient.publicSigning(server.url("/").toString(), "signer-uuid-1");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void otpVerifySignFlow() throws Exception {
        server.enqueue(json("""
                {"title":"NDA","can_sign":true}
                """));
        server.enqueue(json("{}"));
        server.enqueue(json("{\"verified\":true,\"message\":\"ok\"}"));
        server.enqueue(json("""
                {"message":"signed","signer_status":"signed","envelope_completed":false}
                """));

        PublicSigningSession info = session.getSession();
        assertEquals("NDA", info.getTitle());
        assertNull(server.takeRequest().getHeader("Authorization"));

        session.requestOtp();
        assertEquals("/api/v4/public/envelopes/signer-uuid-1/otp", server.takeRequest().getPath());

        PublicOtpVerifyResponse verified = session.verifyOtp("123456");
        assertTrue(verified.isVerified());
        RecordedRequest verify = server.takeRequest();
        assertTrue(verify.getBody().readUtf8().contains("123456"));

        PublicSignResponse signed = session.sign(PublicSignRequest.builder()
                .sealConsent(true)
                .extraMark("doc-uuid", PublicExtraMark.at(0, 10, 20).type(SignatureType.SIGNATURE))
                .build());
        assertEquals("signed", signed.getSignerStatus());
        assertTrue(server.takeRequest().getBody().readUtf8().contains("extra_positions"));
    }

    @Test
    void previewDocumentIncludesSignerQuery() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/pdf")
                .setBody("pdf-bytes"));

        byte[] bytes = session.previewDocument("doc-uuid-1");
        assertEquals("pdf-bytes".length(), bytes.length);
        RecordedRequest request = server.takeRequest();
        assertEquals("/api/v4/public/documents/doc-uuid-1/preview?signer=signer-uuid-1", request.getPath());
        assertNull(request.getHeader("Authorization"));
    }

    @Test
    void refuseWithReason() throws Exception {
        server.enqueue(json("{}"));

        session.refuse("Not for me", null);
        RecordedRequest request = server.takeRequest();
        assertEquals("/api/v4/public/envelopes/signer-uuid-1/refuse", request.getPath());
        assertTrue(request.getBody().readUtf8().contains("Not for me"));
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
