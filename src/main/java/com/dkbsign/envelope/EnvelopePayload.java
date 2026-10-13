package com.dkbsign.envelope;

import com.dkbsign.enums.SignatureLevel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class EnvelopePayload {

    private final String title;
    private final String message;
    private final SigningOrder signingOrder;
    private final SignatureLevel signatureLevel;
    private final List<EnvelopeSigner> signers;
    private final EnvelopePassword password;

    private EnvelopePayload(Builder builder) {
        this.title = builder.title;
        this.message = builder.message;
        this.signingOrder = builder.signingOrder;
        this.signatureLevel = builder.signatureLevel;
        this.signers = List.copyOf(builder.signers);
        this.password = builder.password;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("signing_order")
    public SigningOrder getSigningOrder() {
        return signingOrder;
    }

    @JsonProperty("signature_level")
    public SignatureLevel getSignatureLevel() {
        return signatureLevel;
    }

    @JsonProperty("signers")
    public List<EnvelopeSigner> getSigners() {
        return signers;
    }

    @JsonProperty("password")
    public EnvelopePassword getPassword() {
        return password;
    }

    public static final class Builder {

        private String title;
        private String message;
        private SigningOrder signingOrder;
        private SignatureLevel signatureLevel;
        private final List<EnvelopeSigner> signers = new ArrayList<>();
        private EnvelopePassword password;

        private Builder() {}

        public Builder title(String title) {
            this.title = blankToNull(title);
            return this;
        }

        public Builder message(String message) {
            this.message = blankToNull(message);
            return this;
        }

        public Builder signingOrder(SigningOrder signingOrder) {
            this.signingOrder = signingOrder;
            return this;
        }


        public Builder signatureLevel(SignatureLevel signatureLevel) {
            this.signatureLevel = signatureLevel;
            return this;
        }

        public Builder signer(EnvelopeSigner signer) {
            this.signers.add(Objects.requireNonNull(signer, "signer"));
            return this;
        }

        public Builder password(EnvelopePassword password) {
            this.password = password;
            return this;
        }

        public EnvelopePayload build() {
            if (title == null) {
                throw new IllegalStateException("title() is required");
            }
            if (signers.isEmpty()) {
                throw new IllegalStateException("At least one signer() is required");
            }
            return new EnvelopePayload(this);
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
