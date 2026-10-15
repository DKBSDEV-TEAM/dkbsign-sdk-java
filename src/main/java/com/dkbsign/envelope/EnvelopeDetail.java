package com.dkbsign.envelope;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvelopeDetail {

    private String batchId;
    private String title;
    private String message;
    private EnvelopeStatus status;
    private Boolean cancelled;
    private String accessRevokedAt;
    private List<SignerDetail> signers = Collections.emptyList();

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

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("status")
    public EnvelopeStatus getStatus() {
        return status;
    }

    public void setStatus(EnvelopeStatus status) {
        this.status = status;
    }

    @JsonProperty("cancelled")
    public Boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    @JsonProperty("access_revoked_at")
    public String getAccessRevokedAt() {
        return accessRevokedAt;
    }

    public void setAccessRevokedAt(String accessRevokedAt) {
        this.accessRevokedAt = accessRevokedAt;
    }

    @JsonProperty("signers")
    public List<SignerDetail> getSigners() {
        return signers;
    }

    public void setSigners(List<SignerDetail> signers) {
        this.signers = signers == null ? Collections.emptyList() : signers;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SignerDetail {

        private String uuid;
        private String email;
        private String signingUrl;
        private String status;
        private String identityStatus;

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

        @JsonProperty("identity_status")
        public String getIdentityStatus() {
            return identityStatus;
        }

        public void setIdentityStatus(String identityStatus) {
            this.identityStatus = identityStatus;
        }
    }
}
