package com.dkbsign.envelope;

import com.fasterxml.jackson.databind.JsonNode;


public final class EnvelopeListResult {

    private final JsonNode body;

    public EnvelopeListResult(JsonNode body) {
        this.body = body;
    }

    public JsonNode getBody() {
        return body;
    }
}
