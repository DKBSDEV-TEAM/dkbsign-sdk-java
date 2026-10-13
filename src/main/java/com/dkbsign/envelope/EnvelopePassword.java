package com.dkbsign.envelope;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class EnvelopePassword {

    private final Boolean enabled;
    private final String userPassword;
    private final String ownerPassword;

    public EnvelopePassword(Boolean enabled, String userPassword, String ownerPassword) {
        this.enabled = enabled;
        this.userPassword = userPassword;
        this.ownerPassword = ownerPassword;
    }

    public static EnvelopePassword userPassword(String userPassword) {
        if (userPassword == null || userPassword.length() < 4) {
            throw new IllegalArgumentException("user_password must be at least 4 characters");
        }
        return new EnvelopePassword(true, userPassword, null);
    }

    @JsonProperty("enabled")
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonProperty("user_password")
    public String getUserPassword() {
        return userPassword;
    }

    @JsonProperty("owner_password")
    public String getOwnerPassword() {
        return ownerPassword;
    }
}
