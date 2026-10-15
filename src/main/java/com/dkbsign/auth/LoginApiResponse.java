package com.dkbsign.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class LoginApiResponse {

    private String status;
    private String message;
    private LoginData data;

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("data")
    public LoginData getData() {
        return data;
    }

    public void setData(LoginData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class LoginData {

        private Boolean mfaRequired;
        private String challengeToken;
        private List<String> methods = Collections.emptyList();
        private String emailHint;
        private String accessToken;
        private Boolean mfaSetupRequired;
        private UserProfile user;

        @JsonProperty("mfa_required")
        public Boolean getMfaRequired() {
            return mfaRequired;
        }

        public void setMfaRequired(Boolean mfaRequired) {
            this.mfaRequired = mfaRequired;
        }

        @JsonProperty("challenge_token")
        public String getChallengeToken() {
            return challengeToken;
        }

        public void setChallengeToken(String challengeToken) {
            this.challengeToken = challengeToken;
        }

        @JsonProperty("methods")
        public List<String> getMethods() {
            return methods;
        }

        public void setMethods(List<String> methods) {
            this.methods = methods == null ? Collections.emptyList() : methods;
        }

        @JsonProperty("email_hint")
        public String getEmailHint() {
            return emailHint;
        }

        public void setEmailHint(String emailHint) {
            this.emailHint = emailHint;
        }

        @JsonProperty("access_token")
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        @JsonProperty("mfa_setup_required")
        public Boolean getMfaSetupRequired() {
            return mfaSetupRequired;
        }

        public void setMfaSetupRequired(Boolean mfaSetupRequired) {
            this.mfaSetupRequired = mfaSetupRequired;
        }

        @JsonProperty("user")
        public UserProfile getUser() {
            return user;
        }

        public void setUser(UserProfile user) {
            this.user = user;
        }
    }
}
