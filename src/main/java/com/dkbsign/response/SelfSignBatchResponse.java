package com.dkbsign.response;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Result of signing several PDFs in one call. The API does not document this body yet,
 * so it is exposed as raw JSON.
 */
public final class SelfSignBatchResponse {

    private final int statusCode;
    private final JsonNode body;

    public SelfSignBatchResponse(int statusCode, JsonNode body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /** True on HTTP 207: some documents were signed, others failed. */
    public boolean isPartial() {
        return statusCode == 207;
    }

    public String getMessage() {
        JsonNode message = body == null ? null : body.get("message");
        return message == null || !message.isTextual() ? null : message.asText();
    }

    public JsonNode getBody() {
        return body;
    }
}
