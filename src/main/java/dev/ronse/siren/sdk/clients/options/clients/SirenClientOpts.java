package dev.ronse.siren.sdk.clients.options.clients;

public final class SirenClientOpts {
    public final String apiKey;
    public final String apiUri;

    private SirenClientOpts(Builder builder) {
        apiKey = builder.apiKey;
        apiUri = builder.apiUri;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String apiKey = "";
        private String apiUri = "";

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder apiUri(String apiUri) {
            this.apiUri = apiUri;
            return this;
        }

        public SirenClientOpts build() {
            return new SirenClientOpts(this);
        }
    }
}
