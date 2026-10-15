package com.dkbsign.auth;

import com.dkbsign.DkbSignClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private MockWebServer server;
    private AuthService auth;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        auth = AuthService.create(server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void loginReturnsToken() throws Exception {
        server.enqueue(json("""
                {
                  "status": "success",
                  "data": {
                    "access_token": "tok-abc",
                    "mfa_setup_required": false,
                    "user": { "id": 1, "role": "user", "email": "a@b.test" }
                  }
                }
                """));

        LoginResult result = auth.login("a@b.test", "secret");

        RecordedRequest request = server.takeRequest();
        assertEquals("/api/v1/login", request.getPath());
        assertEquals("POST", request.getMethod());
        assertTrue(request.getBody().readUtf8().contains("a@b.test"));
        assertNull(request.getHeader("Authorization"));

        assertTrue(result.isAuthenticated());
        assertEquals("tok-abc", result.getAccessToken());
        DkbSignClient client = auth.toClient(result);
        assertTrue(client.selfSign() != null);
    }

    @Test
    void loginMfaThenVerify() throws Exception {
        server.enqueue(json("""
                {
                  "status": "success",
                  "data": {
                    "mfa_required": true,
                    "challenge_token": "ch-1",
                    "methods": ["email"],
                    "email_hint": "a***@b.test"
                  }
                }
                """));
        server.enqueue(json("{}"));
        server.enqueue(json("""
                {
                  "status": "success",
                  "data": {
                    "access_token": "tok-mfa",
                    "user": { "id": 1, "role": "user", "email": "a@b.test" }
                  }
                }
                """));

        LoginResult first = auth.login("a@b.test", "secret");
        assertTrue(first.isMfaRequired());
        assertEquals("ch-1", first.getMfaChallenge().getChallengeToken());
        assertEquals("/api/v1/login", server.takeRequest().getPath());

        auth.sendMfaEmail(first.getMfaChallenge().getChallengeToken());
        assertEquals("/api/v1/login/mfa/email/send", server.takeRequest().getPath());

        LoginResult second = auth.verifyMfa("ch-1", MfaMethod.EMAIL, "123456");
        assertEquals("tok-mfa", second.getAccessToken());
        assertFalse(second.isMfaRequired());
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
