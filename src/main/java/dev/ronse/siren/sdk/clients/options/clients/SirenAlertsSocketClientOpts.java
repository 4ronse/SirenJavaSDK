package dev.ronse.siren.sdk.clients.options.clients;

import dev.ronse.siren.sdk.utils.QueryParametersList;
import io.socket.client.IO;
import io.socket.client.SocketOptionBuilder;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.function.Consumer;

import static dev.ronse.siren.sdk.internal.SharedConstants.API_BASE_URI;

public final class SirenAlertsSocketClientOpts {
    public final String apiKey;
    public final URI socketUri;
    public final IO.Options ioOpts;
    public final QueryParametersList queryParametersList;

    private SirenAlertsSocketClientOpts (Builder builder) {
        apiKey = builder.apiKey;
        socketUri = builder.socketUri;
        ioOpts = builder.ioOptsBuilder.build();
        queryParametersList = builder.queryParametersList;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String apiKey = "";
        private URI socketUri = API_BASE_URI;
        private final SocketOptionBuilder ioOptsBuilder = IO.Options.builder();
        private final QueryParametersList queryParametersList = new QueryParametersList();

        private Builder() {}

        public Builder apiKey(@NotNull String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder socketUri(@NotNull URI uri) {
            socketUri = uri;
            return this;
        }

        public Builder ioOpts(Consumer<SocketOptionBuilder> builderConsumer) {
            builderConsumer.accept(ioOptsBuilder);
            return this;
        }

        public Builder query(Consumer<QueryParametersList> queryParametersListConsumer) {
            queryParametersListConsumer.accept(queryParametersList);
            return this;
        }

        public SirenAlertsSocketClientOpts build() {
            if (apiKey.isBlank()) throw new IllegalStateException("API key must be provided");
            return new SirenAlertsSocketClientOpts(this);
        }
    }
}
