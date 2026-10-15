package com.dkbsign.publicenvelope;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;

/** {@code GET /api/v4/public/envelopes/{signer_uuid}} (partial schema). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicSigningSession {

    private String title;
    private String message;
    private Boolean canSign;
    private JsonNode identity;
    private JsonNode features;
    private List<JsonNode> documents = Collections.emptyList();

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("can_sign")
    public Boolean getCanSign() {
        return canSign;
    }

    public void setCanSign(Boolean canSign) {
        this.canSign = canSign;
    }

    @JsonProperty("identity")
    public JsonNode getIdentity() {
        return identity;
    }

    public void setIdentity(JsonNode identity) {
        this.identity = identity;
    }

    @JsonProperty("features")
    public JsonNode getFeatures() {
        return features;
    }

    public void setFeatures(JsonNode features) {
        this.features = features;
    }

    @JsonProperty("documents")
    public List<JsonNode> getDocuments() {
        return documents;
    }

    public void setDocuments(List<JsonNode> documents) {
        this.documents = documents == null ? Collections.emptyList() : documents;
    }
}
