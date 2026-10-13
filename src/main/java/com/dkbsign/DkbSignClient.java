package com.dkbsign;

import com.dkbsign.envelope.EnvelopeService;
import com.dkbsign.http.HttpClient;
import com.dkbsign.signature.SelfSignService;

/** Entry point of the SDK. Thread-safe and meant to be created once and reused. */
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
