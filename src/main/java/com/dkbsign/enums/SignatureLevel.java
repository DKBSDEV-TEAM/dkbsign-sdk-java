package com.dkbsign.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SignatureLevel {
    SIMPLE("simple"),
    ADVANCED("advanced"),
    QUALIFIED("qualified");

    private final String value;

    SignatureLevel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
