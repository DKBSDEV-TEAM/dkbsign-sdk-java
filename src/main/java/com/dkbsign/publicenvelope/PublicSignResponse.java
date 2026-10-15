package com.dkbsign.publicenvelope;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicSignResponse {

    private String message;
    private String signerStatus;
    private boolean envelopeCompleted;

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("signer_status")
    public String getSignerStatus() {
        return signerStatus;
    }

    public void setSignerStatus(String signerStatus) {
        this.signerStatus = signerStatus;
    }

    @JsonProperty("envelope_completed")
    public boolean isEnvelopeCompleted() {
        return envelopeCompleted;
    }

    public void setEnvelopeCompleted(boolean envelopeCompleted) {
        this.envelopeCompleted = envelopeCompleted;
    }
}
