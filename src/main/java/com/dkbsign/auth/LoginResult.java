package com.dkbsign.auth;


public final class LoginResult {

    private final String accessToken;
    private final UserProfile user;
    private final boolean mfaSetupRequired;
    private final MfaChallenge mfaChallenge;

    private LoginResult(String accessToken, UserProfile user, boolean mfaSetupRequired, MfaChallenge mfaChallenge) {
        this.accessToken = accessToken;
        this.user = user;
        this.mfaSetupRequired = mfaSetupRequired;
        this.mfaChallenge = mfaChallenge;
    }

    public static LoginResult authenticated(String accessToken, UserProfile user, boolean mfaSetupRequired) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("accessToken is required");
        }
        return new LoginResult(accessToken, user, mfaSetupRequired, null);
    }

    public static LoginResult mfaRequired(MfaChallenge challenge) {
        return new LoginResult(null, null, false, challenge);
    }

    public boolean isMfaRequired() {
        return mfaChallenge != null;
    }

    public boolean isAuthenticated() {
        return accessToken != null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public UserProfile getUser() {
        return user;
    }

    public boolean isMfaSetupRequired() {
        return mfaSetupRequired;
    }

    public MfaChallenge getMfaChallenge() {
        return mfaChallenge;
    }
}
