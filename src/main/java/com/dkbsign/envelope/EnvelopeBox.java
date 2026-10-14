package com.dkbsign.envelope;

import com.fasterxml.jackson.annotation.JsonValue;

/** Inbox to clear with {@link EnvelopeService#clear(EnvelopeBox)}. */
public enum EnvelopeBox {
    SENT("sent"),
    RECEIVED("received");

    private final String value;

    EnvelopeBox(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
