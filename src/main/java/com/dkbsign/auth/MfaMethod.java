package com.dkbsign.auth;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MfaMethod {
    EMAIL("email"),
    TOTP("totp");

    private final String value;

    MfaMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
