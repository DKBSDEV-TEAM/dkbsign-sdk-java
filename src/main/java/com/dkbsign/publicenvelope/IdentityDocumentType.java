package com.dkbsign.publicenvelope;

import com.fasterxml.jackson.annotation.JsonValue;

public enum IdentityDocumentType {
    CNI("cni"),
    PASSPORT("passport");

    private final String value;

    IdentityDocumentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
