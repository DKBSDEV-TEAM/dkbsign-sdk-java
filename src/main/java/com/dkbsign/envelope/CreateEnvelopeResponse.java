package com.dkbsign.envelope;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateEnvelopeResponse {

    private String message;
    private String batchId;
    private String title;
    private List<SignerResult> signers = Collections.emptyList();

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("batch_id")
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("signers")
    public List<SignerResult> getSigners() {
        return signers;
    }

    public void setSigners(List<SignerResult> signers) {
        this.signers = signers == null ? Collections.emptyList() : signers;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SignerResult {

        private String uuid;
        private String email;
        private String signingUrl;
        private String status;
        private String devOtpCode;

        @JsonProperty("uuid")
        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        @JsonProperty("email")
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @JsonProperty("signing_url")
        public String getSigningUrl() {
            return signingUrl;
        }

        public void setSigningUrl(String signingUrl) {
            this.signingUrl = signingUrl;
        }

        @JsonProperty("status")
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @JsonProperty("dev_otp_code")
        public String getDevOtpCode() {
            return devOtpCode;
        }

        public void setDevOtpCode(String devOtpCode) {
            this.devOtpCode = devOtpCode;
        }
    }
}
