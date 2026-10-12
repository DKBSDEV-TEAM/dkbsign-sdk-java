package com.dkbsign.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SelfSignResponse {

    private String message;
    private String signedPdfUrl;
    private String documentUuid;
    private String verificationUrl;

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("signed_pdf_url")
    public String getSignedPdfUrl() {
        return signedPdfUrl;
    }

    public void setSignedPdfUrl(String signedPdfUrl) {
        this.signedPdfUrl = signedPdfUrl;
    }

    @JsonProperty("document_uuid")
    public String getDocumentUuid() {
        return documentUuid;
    }

    public void setDocumentUuid(String documentUuid) {
        this.documentUuid = documentUuid;
    }

    @JsonProperty("verification_url")
    public String getVerificationUrl() {
        return verificationUrl;
    }

    public void setVerificationUrl(String verificationUrl) {
        this.verificationUrl = verificationUrl;
    }
}
