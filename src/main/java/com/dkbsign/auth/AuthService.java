package com.dkbsign.auth;

import com.dkbsign.DkbSignClient;
import com.dkbsign.http.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Login and MFA ({@code POST /api/v1/login}, …). */
public final class AuthService {

    private static final String LOGIN_PATH = "/api/v1/login";
    private static final String MFA_EMAIL_SEND_PATH = "/api/v1/login/mfa/email/send";
    private static final String MFA_VERIFY_PATH = "/api/v1/login/mfa/verify";

    private final HttpClient httpClient;
    private final String baseUrl;

    private AuthService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = new HttpClient(baseUrl, null);
    }

    public static AuthService create(String baseUrl) {
        return new AuthService(baseUrl);
    }

    public static AuthService create() {
        return create(DkbSignClient.DEFAULT_BASE_URL);
    }

    public LoginResult login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
        LoginApiResponse response = httpClient.postPublicJson(
                LOGIN_PATH,
                Map.of("email", email.trim(), "password", password),
                LoginApiResponse.class);
        return toLoginResult(response);
    }

    /** Resend the email MFA code for an open login challenge. */
    public void sendMfaEmail(String challengeToken) {
        httpClient.postPublicJson(
                MFA_EMAIL_SEND_PATH,
                Map.of("challenge_token", requireChallenge(challengeToken)),
                Void.class);
    }

    public LoginResult verifyMfa(String challengeToken, MfaMethod method, String code) {
        Objects.requireNonNull(method, "method");
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        LoginApiResponse response = httpClient.postPublicJson(
                MFA_VERIFY_PATH,
                Map.of(
                        "challenge_token", requireChallenge(challengeToken),
                        "method", method.getValue(),
                        "code", code.trim()),
                LoginApiResponse.class);
        LoginResult result = toLoginResult(response);
        if (result.isMfaRequired()) {
            throw new IllegalStateException("MFA verify returned another challenge — unexpected API response");
        }
        return result;
    }

    /** Builds an authenticated client from a successful {@link LoginResult}. */
    public DkbSignClient toClient(LoginResult loginResult) {
        if (!loginResult.isAuthenticated()) {
            throw new IllegalStateException("Login is not complete — complete MFA first");
        }
        return DkbSignClient.builder()
                .baseUrl(baseUrl)
                .token(loginResult.getAccessToken())
                .build();
    }

    private static LoginResult toLoginResult(LoginApiResponse response) {
        if (response == null || response.getData() == null) {
            throw new IllegalStateException("Empty login response");
        }
        LoginApiResponse.LoginData data = response.getData();
        if (Boolean.TRUE.equals(data.getMfaRequired())) {
            List<MfaMethod> methods = new ArrayList<>();
            for (String method : data.getMethods()) {
                methods.add(parseMethod(method));
            }
            return LoginResult.mfaRequired(
                    new MfaChallenge(data.getChallengeToken(), methods, data.getEmailHint()));
        }
        return LoginResult.authenticated(
                data.getAccessToken(),
                data.getUser(),
                Boolean.TRUE.equals(data.getMfaSetupRequired()));
    }

    private static MfaMethod parseMethod(String value) {
        if (value == null) {
            throw new IllegalStateException("Unknown MFA method in login response");
        }
        return switch (value) {
            case "email" -> MfaMethod.EMAIL;
            case "totp" -> MfaMethod.TOTP;
            default -> throw new IllegalArgumentException(
                    "Unsupported MFA method: " + value + " (WebAuthn not implemented in this SDK)");
        };
    }

    private static String requireChallenge(String challengeToken) {
        if (challengeToken == null || challengeToken.isBlank()) {
            throw new IllegalArgumentException("challengeToken is required");
        }
        return challengeToken.trim();
    }
}
