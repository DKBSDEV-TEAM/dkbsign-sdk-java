package com.dkbsign.envelope;

import com.dkbsign.DkbSignClient;
import com.dkbsign.enums.SignatureType;
import com.dkbsign.model.SignatureMark;
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

import okio.Buffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvelopeServiceTest {

    private static final Pattern PAYLOAD_PART =
            Pattern.compile("name=\"payload\"\\r\\n.*?\\r\\n\\r\\n(\\{.*?})\\r\\n--", Pattern.DOTALL);

    @TempDir
    Path dir;

    private MockWebServer server;
    private EnvelopeService envelopes;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        envelopes = DkbSignClient.builder()
                .baseUrl(server.url("/").toString())
                .token("tok")
                .build()
                .envelopes();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void createSendsDocumentsPayloadAndAttachments() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "message": "Parapheur créé",
                          "batch_id": "batch-1",
                          "title": "NDA",
                          "signers": [{
                            "uuid": "signer-1",
                            "email": "jane@acme.test",
                            "signing_url": "https://app/sign/signer-1",
                            "status": "pending",
                            "dev_otp_code": "999888"
                          }]
                        }
                        """));

        Path pdf = Files.writeString(dir.resolve("doc.pdf"), "%PDF");
        Path att = Files.writeString(dir.resolve("note.txt"), "annex");

        CreateEnvelopeResponse response = envelopes.create(CreateEnvelopeRequest.builder()
                .document(pdf)
                .attachment(0, att)
                .title("NDA")
                .signer(EnvelopeSigner.builder("jane@acme.test")
                        .mark(0, 0, SignatureMark.at(10, 10).type(SignatureType.SIGNATURE))
                        .build())
                .build());

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readString(StandardCharsets.ISO_8859_1);

        assertEquals("POST", request.getMethod());
        assertEquals("/api/v4/envelopes", request.getPath());
        assertEquals("Bearer tok", request.getHeader("Authorization"));
        assertTrue(body.contains("name=\"documents[]\"; filename=\"doc.pdf\""));
        assertTrue(body.contains("name=\"attachments_0[]\"; filename=\"note.txt\""));
        assertTrue(payload(body).contains("\"title\":\"NDA\""));

        assertEquals("batch-1", response.getBatchId());
        assertEquals("signer-1", response.getSigners().get(0).getUuid());
        assertEquals("999888", response.getSigners().get(0).getDevOtpCode());
    }

    @Test
    void getCancelDeleteAndClear() throws Exception {
        server.enqueue(json(200, """
                {"batch_id":"b-1","title":"NDA","status":"in_progress","signers":[]}
                """));
        server.enqueue(json(200, "{}"));
        server.enqueue(new MockResponse().setResponseCode(204));
        server.enqueue(json(200, "{}"));

        EnvelopeDetail detail = envelopes.get("b-1");
        assertEquals("b-1", detail.getBatchId());
        assertEquals(EnvelopeStatus.IN_PROGRESS, detail.getStatus());
        assertEquals("/api/v4/envelopes/b-1", server.takeRequest().getPath());

        envelopes.cancel("b-1", "Wrong party");
        RecordedRequest cancel = server.takeRequest();
        assertEquals("POST", cancel.getMethod());
        assertEquals("/api/v4/envelopes/b-1/cancel", cancel.getPath());
        assertTrue(cancel.getBody().readUtf8().contains("Wrong party"));

        envelopes.deleteSent("b-1");
        assertEquals("DELETE", server.takeRequest().getMethod());

        envelopes.clear(EnvelopeBox.RECEIVED);
        RecordedRequest clear = server.takeRequest();
        assertEquals("POST", clear.getMethod());
        assertEquals("/api/v4/envelopes/clear", clear.getPath());
        assertTrue(clear.getBody().readUtf8().contains("received"));
    }

    @Test
    void listSentAndDownloadDocument() throws Exception {
        server.enqueue(json(200, "{\"data\":[{\"batch_id\":\"b-1\"}]}"));
        server.enqueue(new MockResponse().setBody(new Buffer().write(new byte[] {37, 80, 68, 70})));

        EnvelopeListResult list = envelopes.listSent();
        assertEquals("b-1", list.getBody().get("data").get(0).get("batch_id").asText());
        assertEquals("/api/v4/envelopes", server.takeRequest().getPath());

        assertArrayEquals(new byte[] {37, 80, 68, 70}, envelopes.downloadDocument("doc-uuid"));
        assertEquals("/api/v4/envelopes/documents/doc-uuid/file", server.takeRequest().getPath());
    }

    private static MockResponse json(int status, String body) {
        return new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private static String payload(String multipartBody) {
        Matcher matcher = PAYLOAD_PART.matcher(multipartBody);
        assertTrue(matcher.find(), "payload part not found");
        return matcher.group(1);
    }
}
