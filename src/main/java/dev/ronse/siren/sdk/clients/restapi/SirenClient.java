package dev.ronse.siren.sdk.clients.restapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import dev.ronse.siren.sdk.clients.options.clients.SirenClientOpts;
import dev.ronse.siren.sdk.internal.JsonSupport;
import dev.ronse.siren.sdk.internal.SharedConstants;
import dev.ronse.siren.sdk.utils.QueryParametersList;
import dev.ronse.siren.sdk.utils.StringUtils;
import dev.ronse.siren.sdk.wrappers.AlertType;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class SirenClient {

    private static final Logger LOGGER = Logger.getLogger(SirenClient.class.getName());

    private static final String API_KEY_HEADER = "X-API-Key";

    // Core
    private final String apiKey;
    private final URI baseURI;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    // Sub Clients
    private final StatsClient statsClient;
    private final DataClient dataClient;

    /**
     * Creates a SirenClient
     */
    private SirenClient(SirenClientOpts opts) {
        if(opts.apiKey.isBlank())
            LOGGER.log(Level.WARNING, () -> "API Key is blank!");
        if(opts.apiUri.isBlank())
            LOGGER.log(Level.WARNING, () -> "API URL Was not provided! Connection WILL fail!");

        this.apiKey = opts.apiKey;
        this.baseURI = URI.create(opts.apiUri);
        this.client = createHttpClient();
        this.objectMapper = JsonSupport.getObjectMapper();

        statsClient = StatsClient.fromSirenClient(this);
        dataClient = DataClient.fromSirenClient(this);
    }

    public static SirenClient build(Consumer<SirenClientOpts.Builder> builderConsumer) {
        var builder = SirenClientOpts.builder();
        builderConsumer.accept(builder);
        return new SirenClient(builder.build());
    }

    // ------------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------------

    public StatsClient stats() {
        return statsClient;
    }

    public DataClient data() {
        return dataClient;
    }

    public boolean isHealthy() throws IOException {
        return fetchText("/health").equalsIgnoreCase("ok");
    }

    public Map<AlertType, List<String>> active() throws IOException {
        return get("/active", QueryParametersList.EMPTY, new TypeReference<>() {});
    }

    // ------------------------------------------------------------------------
    // Package Access
    // ------------------------------------------------------------------------

    @NotNull
    URI getURIRelative(String path) {
        return baseURI.resolve(path);
    }

    // ------------------------------------------------------------------------
    // GET Helpers
    // ------------------------------------------------------------------------

    <T> T get(String path, QueryParametersList params, Class<T> type) throws IOException {
        return objectMapper.readValue(
                fetchText(buildGetRequest(path, params)),
                type
        );
    }

    <T> T get(String path, Class<T> type) throws IOException {
        return get(path, QueryParametersList.EMPTY, type);
    }

    <T> T get(String path, QueryParametersList params, TypeReference<T> type) throws IOException {
        return objectMapper.readValue(
                fetchText(buildGetRequest(path, params)),
                type
        );
    }

    <T> T get(String path, TypeReference<T> type) throws IOException {
        return get(path, QueryParametersList.EMPTY, type);
    }

    String fetchText(String path) throws IOException {
        return fetchText(path, QueryParametersList.EMPTY);
    }

    String fetchText(String path, QueryParametersList params) throws IOException {
        return fetchText(buildGetRequest(path, params));
    }

    // Request Construction
    private Request buildGetRequest(String path, QueryParametersList params) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.get(getURIRelative(path))).newBuilder();
        params.addToHttpUrlBuilder(urlBuilder);
        return new Request.Builder()
                .url(urlBuilder.build())
                .addHeader(API_KEY_HEADER, apiKey)
                .get()
                .build();
    }

    private String fetchText(Request request) throws IOException {
        long start = System.nanoTime();
        HttpUrl url = request.url();

        LOGGER.fine(() -> String.format("HTTP %s %s", request.method(), url));

        try (Response response = client.newCall(request).execute()) {
            long duration = (System.nanoTime() - start) / 1_000_000;
            String body = response.body().string();

            if(!response.isSuccessful()) {
                LOGGER.warning(() -> String.format(
                        """
                        API Request Failed
                            Method      : %s
                            URL         : %s
                            Status      : %d
                            Duration    : %d ms
                            Body        : %s
                        """,
                        request.method(), url, response.code(), duration, StringUtils.truncate(body)
                ));

                throw new IOException(String.format("HTTP %d returned : %s", response.code(), url));
            }

            LOGGER.finer(() -> String.format(
                    """
                    API Request Completed
                        Method      : %s
                        URL         : %s
                        Status      : %d
                        Duration    : %d ms
                        Size        : %d bytes
                        Content     : %s
                    """,
                    request.method(), url, response.code(), duration, body.length(), StringUtils.truncate(body)
            ));

            return body;
        }
    }

    // ----------------------------------
    // Utilities
    // ----------------------------------

    private static OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .readTimeout(Duration.ofSeconds(SharedConstants.READ_TIMEOUT_SECONDS))
                .build();
    }
}