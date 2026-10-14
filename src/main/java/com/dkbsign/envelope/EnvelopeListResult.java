package com.dkbsign.envelope;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Raw list response from {@code GET /api/v4/envelopes} or {@code .../received}.
 * The API does not document the list shape yet — inspect {@link #getBody()}.
 */
public final class EnvelopeListResult {

    private final JsonNode body;

    public EnvelopeListResult(JsonNode body) {
        this.body = body;
    }

    public JsonNode getBody() {
        return body;
    }
}
