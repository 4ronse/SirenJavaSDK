package dev.ronse.siren.sdk.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.ronse.siren.sdk.clients.options.statistics.*;
import dev.ronse.siren.sdk.clients.options.statistics.enums.DistributionGroupBy;
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

    public CitiesModel cities(CitiesOpts opts) throws IOException {
        return client.get("/stats/cities", opts.toQueryParams(), CitiesModel.class);
    }

    /**
     * Retrieves alert distribution data grouped by category.
     *
     * <pre>{@code
     * // With options
     * client.stats().distributionByCategory(new DistributionOpts()
     *         .from(Instant.now().minus(7, DAYS))
     *         .to(Instant.now()));
     *
     * // Without options
     * client.stats().distributionByCategory();
     * }</pre>
     *
     * @param opts distribution query options
     * @return a {@link DistributionModel} keyed by {@link AlertType}
     * @throws IOException if the request fails
     */
    public DistributionModel<AlertType> distributionByCategory(DistributionOpts opts) throws IOException {
        return client.get(
                "/stats/distribution",
                opts.toQueryParams(),
                new TypeReference<>() {}
        );
    }

    /**
     * Retrieves alert distribution data grouped by category with default options.
     *
     * @return a {@link DistributionModel} keyed by {@link AlertType}
     * @throws IOException if the request fails
     * @see #distributionByCategory(DistributionOpts)
     */
    public DistributionModel<AlertType> distributionByCategory() throws IOException {
        return distributionByCategory(new DistributionOpts());
    }

    /**
     * Retrieves alert distribution data grouped by origin.
     *
     * <pre>{@code
     * // With options
     * client.stats().distributionByOrigin(new DistributionOpts()
     *         .from(Instant.now().minus(7, DAYS))
     *         .to(Instant.now()));
     *
     * // Without options
     * client.stats().distributionByOrigin();
     * }</pre>
     *
     * @param opts distribution query options (the {@code groupBy} field is overridden to
     *             {@link DistributionGroupBy#ORIGIN})
     * @return a {@link DistributionModel} keyed by origin string
     * @throws IOException if the request fails
     */
    public DistributionModel<String> distributionByOrigin(DistributionOpts opts) throws IOException {
        return client.get(
                "/stats/distribution",
                opts.groupBy(DistributionGroupBy.ORIGIN).toQueryParams(),
                new TypeReference<>() {}
        );
    }

    /**
     * Retrieves alert distribution data grouped by origin with default options.
     *
     * @return a {@link DistributionModel} keyed by origin string
     * @throws IOException if the request fails
     * @see #distributionByOrigin(DistributionOpts)
     */
    public DistributionModel<String> distributionByOrigin() throws IOException {
        return distributionByOrigin(new DistributionOpts());
    }

    /**
     * Retrieves historical alert data.
     *
     * <pre>{@code
     * // With options
     * client.stats().history(new HistoryOpts()
     *         .from(Instant.now().minus(30, DAYS))
     *         .to(Instant.now())
     *         .limit(100));
     *
     * // Without options
     * client.stats().history();
     * }</pre>
     *
     * @param opts history query options
     * @return a {@link HistoryModel} containing the historical records
     * @throws IOException if the request fails
     */
    public HistoryModel history(HistoryOpts opts) throws IOException {
        return client.get("/stats/history", opts.toQueryParams(), HistoryModel.class);
    }

    /**
     * Retrieves historical alert data with default options.
     *
     * @return a {@link HistoryModel} containing the historical records
     * @throws IOException if the request fails
     * @see #history(HistoryOpts)
     */
    public HistoryModel history() throws IOException {
        return history(new HistoryOpts());
    }

    /**
     * Retrieves a list of incidents.
     *
     * <pre>{@code
     * // City is REQUIRED
     * client.stats().incidents(new IncidentsOpts("תל אביב"));
     * }</pre>
     *
     * @param opts incident query options
     * @return an {@link IncidentsModel} containing the matching incidents
     * @throws IOException if the request fails
     */
    public IncidentsModel incidents(IncidentsOpts opts) throws IOException {
        return client.get("/stats/incidents", opts.toQueryParams(), IncidentsModel.class);
    }

    /**
     * Retrieves an aggregated summary.
     *
     * <pre>{@code
     * // With options
     * client.stats().summary(new SummaryOpts()
     *         .from(Instant.now().minus(24, HOURS))
     *         .to(Instant.now()));
     *
     * // Without options
     * client.stats().summary();
     * }</pre>
     *
     * @param opts summary query options
     * @return a {@link SummaryModel} containing the aggregated data
     * @throws IOException if the request fails
     */
    public SummaryModel summary(SummaryOpts opts) throws IOException {
        return client.get("/stats/summary", opts.toQueryParams(), SummaryModel.class);
    }

    /**
     * Retrieves an aggregated summary with default options.
     *
     * @return a {@link SummaryModel} containing the aggregated data
     * @throws IOException if the request fails
     * @see #summary(SummaryOpts)
     */
    public SummaryModel summary() throws IOException {
        return summary(new SummaryOpts());
    }
}
