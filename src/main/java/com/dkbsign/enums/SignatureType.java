package com.dkbsign.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SignatureType {
    SIGNATURE("signature"),
    INITIALS("initials"),
    SEAL("seal"),
    QRCODE("qrcode"),
    DATE("date"),
    TEXT("text"),
    APPROVAL("approval");

    private final String value;

    SignatureType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
