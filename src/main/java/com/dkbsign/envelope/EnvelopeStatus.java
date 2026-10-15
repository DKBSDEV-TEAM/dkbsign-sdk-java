package com.dkbsign.envelope;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum EnvelopeStatus {
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    REFUSED("refused"),
    CANCELLED("cancelled");

    private final String value;

    EnvelopeStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EnvelopeStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (EnvelopeStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown envelope status: " + value);
    }
}
