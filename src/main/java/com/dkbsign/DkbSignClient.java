package com.dkbsign;

import com.dkbsign.http.HttpClient;
import com.dkbsign.signature.SelfSignService;


public final class DkbSignClient {

    public static final String DEFAULT_BASE_URL = "https://api.dkbsigns.com";

    private final SelfSignService selfSign;

    private DkbSignClient(Builder builder) {
        HttpClient httpClient = new HttpClient(builder.baseUrl, builder.token);
        this.selfSign = new SelfSignService(httpClient);
    }

    public static Builder builder() {
        return new Builder();
    }

    public SelfSignService selfSign() {
        return selfSign;
    }

    public static final class Builder {

        private String baseUrl = DEFAULT_BASE_URL;
        private String token;

        private Builder() {}

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }


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
