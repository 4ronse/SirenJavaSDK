package dev.ronse.siren.sdk.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ronse.siren.sdk.clients.options.data.ShelterSearchOpts;
import dev.ronse.siren.sdk.utils.QueryParametersList;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

public final class SirenClient {

    private @NotNull
    final URI baseURI;
    private @NotNull
    final OkHttpClient client;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    private final StatsClient statsClient;
    private final DataClient dataClient;

    /**
     * Creates a SirenClient with the default API base URL.
     *
     * @param apiKey Your Siren API key
     */
    public SirenClient(@NotNull String apiKey) {
        this(apiKey, "https://api.siren.co.il");
    }

    /**
     * Creates a SirenClient with a custom base URL.
     * Useful for testing against a mock or staging server.
     *
     * @param apiKey  Your Siren API key
     * @param baseURI Base URL of the Siren API
     */
    public SirenClient(@NotNull String apiKey, @NotNull String baseURI) {
        if (apiKey.isBlank()) throw new IllegalArgumentException("API key cannot be blank");

        this.apiKey = apiKey;
        this.baseURI = URI.create(baseURI);
        this.client = new OkHttpClient.Builder()
                .readTimeout(Duration.ofSeconds(1))
                .build();

        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        statsClient = StatsClient.fromSirenClient(this);
        dataClient = DataClient.fromSirenClient(this);
    }

    // -------------------------
    // Sub-clients
    // -------------------------

    /**
     * Access statistics endpoints - history, cities, distribution, summary, incidents.
     *
     * <pre>{@code
     * client.stats().history();
     * client.stats().cities(new CitiesOptions().limit(10));
     * }</pre>
     */
    public StatsClient stats() {
        return statsClient;
    }

    /**
     * Access data endpoints - shelter search, cities catalog.
     *
     * <pre>{@code
     * client.data().cities();
     * client.data().shelterSearch(new ShelterSearchOpts(...));
     * }</pre>
     */
    public DataClient data() {
        return dataClient;
    }

    // -------------------------
    // Health
    // -------------------------

    /**
     * Pings the API to check if it is reachable and healthy.
     *
     * @return true if the API responds with OK
     */
    public boolean isHealthy() throws IOException {
        return getString("/health").equalsIgnoreCase("ok");
    }

    // -------------------------
    // Internal HTTP
    // -------------------------

    /**
     * @hidden - not part of the public API
     */
    @NotNull URI getURIRelative(String path) {
        return baseURI.resolve(path);
    }

    /**
     * @hidden - not part of the public API
     */
    @NotNull OkHttpClient getClient() {
        return client;
    }

    /**
     * @hidden - not part of the public API
     */
    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * @hidden - not part of the public API
     */
    private <T> T execute(Request request, Class<T> type) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected response code: " + response.code() + "\n" + response.body().string());

            ResponseBody body = response.body();
            return objectMapper.readValue(body.string(), type);
        }
    }

    /** @hidden — not part of the public API */
    private <T> T execute(Request request, TypeReference<T> type) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected response code: " + response.code());

            ResponseBody body = response.body();

            return objectMapper.readValue(body.string(), type);
        }
    }

    /**
     * @hidden - not part of the public API
     */
    public <T> T get(String path, QueryParametersList params, Class<T> type) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.get(getURIRelative(path)).newBuilder();
        params.addToHttpUrlBuilder(urlBuilder);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("X-API-Key", apiKey)
                .get()
                .build();

        return execute(request, type);
    }

    /**
     * @hidden - not part of the public API
     */
    public <T> T get(String path, Class<T> type) throws IOException {
        return get(path, QueryParametersList.EMPTY, type);
    }

    /** @hidden — not part of the public API */
    public <T> T get(String path, QueryParametersList params, TypeReference<T> type) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.get(getURIRelative(path)).newBuilder();
        params.addToHttpUrlBuilder(urlBuilder);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("X-API-Key", apiKey)
                .get()
                .build();

        return execute(request, type);
    }

    /** @hidden — not part of the public API */
    public <T> T get(String path, TypeReference<T> type) throws IOException {
        return get(path, QueryParametersList.EMPTY, type);
    }

    /**
     * @hidden - not part of the public API
     */
    public String getString(String path, QueryParametersList params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.get(getURIRelative(path)).newBuilder();
        params.addToHttpUrlBuilder(urlBuilder);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected response code: " + response.code());

            ResponseBody body = response.body();
            return body.string();
        }
    }

    /**
     * @hidden - not part of the public API
     */
    public String getString(String path) throws IOException {
        return getString(path, QueryParametersList.EMPTY);
    }
}