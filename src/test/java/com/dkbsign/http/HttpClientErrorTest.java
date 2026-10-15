package com.dkbsign.http;

import com.dkbsign.exception.DkbSignException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpClientErrorTest {

    private MockWebServer server;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new HttpClient(server.url("/").toString(), "token");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void gatewayEnvelope() {
        DkbSignException e = fail(401, "{\"status\":\"error\",\"message\":\"Invalid authentication token\"}");

        assertEquals(401, e.getStatusCode());
        assertNull(e.getErrorCode());
        assertEquals("Invalid authentication token", e.getApiMessage());
        assertTrue(e.isUnauthorized());
    }

    @Test
    void plainErrorString() {
        DkbSignException e = fail(404, "{\"error\":\"Lien de signature invalide ou expiré\"}");
        assertEquals("Lien de signature invalide ou expiré", e.getApiMessage());
    }

    @Test
    void signingErrorWithErrorCode() {
        DkbSignException e = fail(400, "{\"error\":\"Anchor missing\",\"error_code\":\"ANCHOR_NOT_FOUND\"}");

        assertEquals("ANCHOR_NOT_FOUND", e.getErrorCode());
        assertEquals("Anchor missing", e.getApiMessage());
        assertTrue(e.getMessage().contains("[ANCHOR_NOT_FOUND]"));
    }

    @Test
    void nestedErrorObject() {
        DkbSignException e = fail(422, "{\"error\":{\"code\":\"INVALID_DOCUMENT\",\"message\":\"Bad PDF\"}}");

        assertEquals("INVALID_DOCUMENT", e.getErrorCode());
        assertEquals("Bad PDF", e.getApiMessage());
    }

    @Test
    void validationErrorsAcceptNullValues() {
        DkbSignException e = fail(422,
                "{\"message\":\"Validation failed\",\"errors\":{\"params\":[\"required\"],\"x\":null}}");

        assertEquals("Validation failed", e.getApiMessage());
        assertEquals(2, e.getFieldErrors().size());
        assertTrue(e.getFieldErrors().containsKey("x"));
        assertNull(e.getFieldErrors().get("x"));
    }

    @Test
    void nonJsonErrorKeepsATruncatedBody() {
        char[] html = new char[2000];
        Arrays.fill(html, 'a');
        DkbSignException e = fail(503, "<html>" + new String(html) + "</html>");

        assertEquals(503, e.getStatusCode());
        assertNull(e.getApiMessage());
        assertTrue(e.isRetryable());
        assertTrue(e.getMessage().length() < 700);
        assertTrue(e.getResponseBody().length() > 2000);
    }

    @Test
    void unreadableSuccessBody() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("not json"));

        DkbSignException e = assertThrows(DkbSignException.class,
                () -> client.postJson("/x", null, Map.class));
        assertTrue(e.getMessage().startsWith("Unreadable API response"));
        assertFalse(e.isUnauthorized());
    }

    private DkbSignException fail(int status, String body) {
        server.enqueue(new MockResponse().setResponseCode(status).setBody(body));
        return assertThrows(DkbSignException.class, () -> client.postJson("/x", null, Map.class));
    }
}
