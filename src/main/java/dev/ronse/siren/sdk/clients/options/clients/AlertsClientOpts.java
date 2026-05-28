package dev.ronse.siren.sdk.clients.options.clients;

import io.socket.client.IO;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public final class AlertsClientOpts {

    private final @Nullable URI baseURL;
    private final IO.Options ioOpts;

    private AlertsClientOpts(@Nullable URI baseURL, IO.Options ioOpts) {
        this.baseURL = baseURL;
        this.ioOpts = ioOpts;
    }

    public @Nullable URI getBaseURL() {
        return baseURL;
    }

    public IO.Options getIoOpts() {
        return ioOpts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private URI baseURL;
        private IO.Options ioOpts;

        public Builder baseURL(URI baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        public Builder baseURL(String baseURL) {
            this.baseURL = URI.create(baseURL);
            return this;
        }

        public Builder ioOpts(IO.Options ioOpts) {
            this.ioOpts = ioOpts;
            return this;
        }

        public AlertsClientOpts build() {
            return new AlertsClientOpts(baseURL, ioOpts != null ? ioOpts : new IO.Options());
        }
    }
}
