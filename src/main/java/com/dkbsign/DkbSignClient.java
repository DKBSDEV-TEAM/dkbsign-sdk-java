package com.dkbsign;

import com.dkbsign.envelope.EnvelopeService;
import com.dkbsign.http.HttpClient;
import com.dkbsign.publicenvelope.PublicEnvelopeService;
import com.dkbsign.signature.SelfSignService;


public final class DkbSignClient {

    public static final String DEFAULT_BASE_URL = "https://api.dkbsigns.com";

    private final SelfSignService selfSign;
    private final EnvelopeService envelopes;

    private DkbSignClient(Builder builder) {
        HttpClient httpClient = new HttpClient(builder.baseUrl, builder.token);
        this.selfSign = new SelfSignService(httpClient);
        this.envelopes = new EnvelopeService(httpClient);
    }

    public static Builder builder() {
        return new Builder();
    }

    public SelfSignService selfSign() {
        return selfSign;
    }

    public EnvelopeService envelopes() {
        return envelopes;
    }

    /**
     * Public signer flow (no login). {@code signerUuid} is the UUID from {@code signing_url}
     * ({@code /api/v4/public/envelopes/{signer_uuid}}).
     */
    public static PublicEnvelopeService publicSigning(String signerUuid) {
        return PublicEnvelopeService.create(signerUuid);
    }

    public static PublicEnvelopeService publicSigning(String baseUrl, String signerUuid) {
        return PublicEnvelopeService.create(baseUrl, signerUuid);
    }

    public static final class Builder {

        private String baseUrl = DEFAULT_BASE_URL;
        private String token;

        private Builder() {}

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /** Bearer token returned by {@code POST /api/v1/login}. */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public DkbSignClient build() {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("token is required");
            }
            return new DkbSignClient(this);
        }
    }
}
