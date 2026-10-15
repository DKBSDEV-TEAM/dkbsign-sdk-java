package com.dkbsign.auth;

import java.util.List;


public final class MfaChallenge {

    private final String challengeToken;
    private final List<MfaMethod> methods;
    private final String emailHint;

    public MfaChallenge(String challengeToken, List<MfaMethod> methods, String emailHint) {
        this.challengeToken = challengeToken;
        this.methods = List.copyOf(methods);
        this.emailHint = emailHint;
    }

    public String getChallengeToken() {
        return challengeToken;
    }

    public List<MfaMethod> getMethods() {
        return methods;
    }

    public String getEmailHint() {
        return emailHint;
    }
}
