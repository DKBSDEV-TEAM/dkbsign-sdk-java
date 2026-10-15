package com.dkbsign.publicenvelope;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PublicSignRequest {

    private final String signatureImageBase64;
    private final String initialsImageBase64;
    private final String sealImageBase64;
    private final Boolean sealConsent;
    private final String mfaCode;
    private final String secondOtpCode;
    private final Map<String, List<PublicExtraMark>> extraPositions;

    private PublicSignRequest(Builder builder) {
        this.signatureImageBase64 = builder.signatureImageBase64;
        this.initialsImageBase64 = builder.initialsImageBase64;
        this.sealImageBase64 = builder.sealImageBase64;
        this.sealConsent = builder.sealConsent;
        this.mfaCode = builder.mfaCode;
        this.secondOtpCode = builder.secondOtpCode;
        Map<String, List<PublicExtraMark>> copy = new LinkedHashMap<>();
        builder.extraPositions.forEach((k, v) -> copy.put(k, List.copyOf(v)));
        this.extraPositions = copy.isEmpty() ? null : Map.copyOf(copy);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("signature_image_base64")
    public String getSignatureImageBase64() {
        return signatureImageBase64;
    }

    @JsonProperty("initials_image_base64")
    public String getInitialsImageBase64() {
        return initialsImageBase64;
    }

    @JsonProperty("seal_image_base64")
    public String getSealImageBase64() {
        return sealImageBase64;
    }

    @JsonProperty("seal_consent")
    public Boolean getSealConsent() {
        return sealConsent;
    }

    @JsonProperty("mfa_code")
    public String getMfaCode() {
        return mfaCode;
    }

    @JsonProperty("second_otp_code")
    public String getSecondOtpCode() {
        return secondOtpCode;
    }

    @JsonProperty("extra_positions")
    public Map<String, List<PublicExtraMark>> getExtraPositions() {
        return extraPositions;
    }

    public static final class Builder {

        private static final int MAX_MARKS_PER_DOCUMENT = 50;

        private String signatureImageBase64;
        private String initialsImageBase64;
        private String sealImageBase64;
        private Boolean sealConsent;
        private String mfaCode;
        private String secondOtpCode;
        private final Map<String, List<PublicExtraMark>> extraPositions = new LinkedHashMap<>();

        public Builder signatureImageBase64(String value) {
            this.signatureImageBase64 = blankToNull(value);
            return this;
        }

        public Builder initialsImageBase64(String value) {
            this.initialsImageBase64 = blankToNull(value);
            return this;
        }

        public Builder sealImageBase64(String value) {
            this.sealImageBase64 = blankToNull(value);
            return this;
        }

        public Builder sealConsent(boolean consent) {
            this.sealConsent = consent;
            return this;
        }

        public Builder mfaCode(String value) {
            this.mfaCode = blankToNull(value);
            return this;
        }

        public Builder secondOtpCode(String value) {
            this.secondOtpCode = blankToNull(value);
            return this;
        }

        public Builder extraMark(String documentUuid, PublicExtraMark mark) {
            if (documentUuid == null || documentUuid.isBlank()) {
                throw new IllegalArgumentException("documentUuid is required");
            }
            List<PublicExtraMark> marks =
                    extraPositions.computeIfAbsent(documentUuid, k -> new java.util.ArrayList<>());
            if (marks.size() >= MAX_MARKS_PER_DOCUMENT) {
                throw new IllegalArgumentException("At most " + MAX_MARKS_PER_DOCUMENT + " marks per document");
            }
            marks.add(mark);
            return this;
        }

        public PublicSignRequest build() {
            return new PublicSignRequest(this);
        }

        private static String blankToNull(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}
