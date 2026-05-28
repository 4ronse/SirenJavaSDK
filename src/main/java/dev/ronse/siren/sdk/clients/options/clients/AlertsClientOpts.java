package dev.ronse.siren.sdk.clients.options.clients;

import dev.ronse.siren.sdk.utils.QueryParametersList;
import io.socket.client.IO;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public final class AlertsClientOpts {

    private final @Nullable URI baseURL;
    private final IO.Options ioOpts;
    private final QueryParametersList query;

    private AlertsClientOpts(@Nullable URI baseURL, IO.Options ioOpts, QueryParametersList query) {
        this.baseURL = baseURL;
        this.ioOpts = ioOpts;
        this.query = query;
    }

    public @Nullable URI getBaseURL() {
        return baseURL;
    }

    public IO.Options getIoOpts() {
        return ioOpts;
    }

    public QueryParametersList query() {
        return query;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private URI baseURL;
        private IO.Options ioOpts;
        private QueryParametersList query = new QueryParametersList();

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

        public Builder setQueryParameter(String k, String v) {
            query.set(k, v);
            return this;
        }

        public <T> Builder setQueryParameter(String k, T v) {
            return setQueryParameter(k, String.valueOf(v));
        }

        public Builder setQueryParameter(String k, Enum<?> v) {
            return setQueryParameter(k, v.toString());
        }

        public Builder unsetQueryParameter(String k) {
            query.unset(k);
            return this;
        }

        public AlertsClientOpts build() {
            return new AlertsClientOpts(baseURL, ioOpts != null ? ioOpts : new IO.Options(), query);
        }
    }
}
