package com.dkbsign.envelope;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SigningOrder {
    ORDERED("ordered"),
    ANY("any");

    private final String value;

    SigningOrder(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
