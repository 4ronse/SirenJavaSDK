package dev.ronse.siren.sdk.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.ronse.siren.sdk.clients.options.statistics.*;
import dev.ronse.siren.sdk.clients.options.statistics.enums.DistributionGroupBy;
import dev.ronse.siren.sdk.model.shared.PagedResponse;
import dev.ronse.siren.sdk.model.statistics.*;
import dev.ronse.siren.sdk.wrappers.AlertType;

import java.io.IOException;

public final class StatsClient {
    static StatsClient fromSirenClient(SirenClient client) {
        return new StatsClient(client);
    }

    private final SirenClient client;

    private StatsClient(SirenClient client) {
        this.client = client;
    }

    /**
     * Alert counts broken down by city.
     * <p>
     * By default, responses are lean - only city name, zone, and count. Use the
     * {@code include} parameter to opt-in to extra fields like translations and
     * coordinates, keeping bandwidth low when you don't need them.
     *
     * <pre>{@code
     * client.stats().cities(new CitiesOpts()
     *         .search("תל אביב")
     *         .include(DataInclude.TRANSLATIONS, DataInclude.COORDS));
     * }</pre>
     *
     * @param opts query options
     * @throws IOException if the request fails
     */
    public PagedResponse<CitiesModel> cities(CitiesOpts opts) throws IOException {
        return client.get("/stats/cities", opts.toQueryParams(), new TypeReference<>() {});
    }

    public PagedResponse<CitiesModel> cities() throws IOException {
        return cities(new CitiesOpts());
    }

    /**
     * Alert distribution grouped by type (category) or origin source.
     * <p>
     * Useful for building pie charts and computing percentage breakdowns.
     * The response includes {@code totalAlerts} so percentages can be calculated
     * client-side without extra requests.
     *
     * <pre>{@code
     * client.stats().distributionByCategory(new DistributionOpts()
     *         .startDate(Instant.now().minus(7, DAYS))
     *         .endDate(Instant.now()));
     * }</pre>
     *
     * @param opts query options
     * @return distribution keyed by {@link AlertType}
     * @throws IOException if the request fails
     * @see #distributionByOrigin(DistributionOpts)
     */
    public DistributionModel<AlertType> distributionByCategory(DistributionOpts opts) throws IOException {
        return client.get("/stats/distribution", opts.toQueryParams(), new TypeReference<>() {});
    }

    public DistributionModel<AlertType> distributionByCategory() throws IOException {
        return distributionByCategory(new DistributionOpts());
    }

    /**
     * Alert distribution grouped by origin source.
     * <p>
     * Same as {@link #distributionByCategory(DistributionOpts)} but keyed by origin
     * string (e.g. "gaza", "lebanon") instead of alert type. The {@code groupBy}
     * field in opts is overridden automatically.
     *
     * <pre>{@code
     * client.stats().distributionByOrigin(new DistributionOpts()
     *         .startDate(Instant.now().minus(7, DAYS))
     *         .endDate(Instant.now()));
     * }</pre>
     *
     * @param opts query options - {@code groupBy} is ignored and overridden to ORIGIN
     * @return distribution keyed by origin string
     * @throws IOException if the request fails
     * @see #distributionByCategory(DistributionOpts)
     */
    public DistributionModel<String> distributionByOrigin(DistributionOpts opts) throws IOException {
        return client.get("/stats/distribution",
                opts.groupBy(DistributionGroupBy.ORIGIN).toQueryParams(),
                new TypeReference<>() {});
    }

    public DistributionModel<String> distributionByOrigin() throws IOException {
        return distributionByOrigin(new DistributionOpts());
    }

    /**
     * Detailed historical alert records with full city data.
     * <p>
     * Each record includes the list of cities targeted simultaneously. By default,
     * only city IDs and names are returned - use the {@code include} parameter to
     * add translations and coordinates as needed.
     *
     * <pre>{@code
     * client.stats().history(new HistoryOpts()
     *         .startDate(Instant.now().minus(30, DAYS))
     *         .endDate(Instant.now())
     *         .limit(100));
     * }</pre>
     *
     * @param opts query options
     * @throws IOException if the request fails
     */
    public PagedResponse<HistoryModel> history(HistoryOpts opts) throws IOException {
        return client.get("/stats/history", opts.toQueryParams(), new TypeReference<>() {});
    }

    public PagedResponse<HistoryModel> history() throws IOException {
        return history(new HistoryOpts());
    }

    /**
     * Incident analysis for a specific city.
     * <p>
     * For each newsFlash wave, determines whether a real alert followed within a
     * configurable timeout window. Useful for measuring false-alarm rates and
     * understanding alert patterns per city.
     *
     * <pre>{@code
     * // city is required
     * client.stats().incidents(new IncidentsOpts("תל אביב")
     *         .timeoutMinutes(10));
     * }</pre>
     *
     * @param opts query options - city is required
     * @throws IOException if the request fails
     */
    public IncidentsModel incidents(IncidentsOpts opts) throws IOException {
        return client.get("/stats/incidents", opts.toQueryParams(), IncidentsModel.class);
    }

    /**
     * High-level overview of the alert system.
     * <p>
     * By default returns core counts and unique city/zone numbers. Use the
     * {@code include} parameter to opt-in to top cities, top zones, a time-series
     * timeline, and peak detection - useful for building dashboards with a single call.
     *
     * <pre>{@code
     * client.stats().summary(new SummaryOpts()
     *         .startDate(Instant.now().minus(24, HOURS))
     *         .endDate(Instant.now())
     *         .include(SummaryInclude.TOP_CITIES, SummaryInclude.TIMELINE));
     * }</pre>
     *
     * @param opts query options
     * @throws IOException if the request fails
     */
    public SummaryModel summary(SummaryOpts opts) throws IOException {
        return client.get("/stats/summary", opts.toQueryParams(), SummaryModel.class);
    }

    public SummaryModel summary() throws IOException {
        return summary(new SummaryOpts());
    }
}
