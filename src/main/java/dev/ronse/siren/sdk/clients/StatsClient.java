package dev.ronse.siren.sdk.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.ronse.siren.sdk.clients.options.statistics.*;
import dev.ronse.siren.sdk.clients.options.statistics.enums.DistributionGroupBy;
import dev.ronse.siren.sdk.model.statistics.*;
import dev.ronse.siren.sdk.wrappers.AlertType;

import java.io.IOException;

// TODO: Add no opts method calls

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
     * client.stats().distributionByCategory(new DistributionOpts()
     *         .from(Instant.now().minus(7, DAYS))
     *         .to(Instant.now()));
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
     * Retrieves alert distribution data grouped by origin.
     *
     * <pre>{@code
     * client.stats().distributionByOrigin(new DistributionOpts()
     *         .from(Instant.now().minus(7, DAYS))
     *         .to(Instant.now()));
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
     * Retrieves historical alert data.
     *
     * <pre>{@code
     * client.stats().history(new HistoryOpts()
     *         .from(Instant.now().minus(30, DAYS))
     *         .to(Instant.now())
     *         .limit(100));
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
     * Retrieves a list of incidents.
     *
     * <pre>{@code
     * client.stats().incidents(new IncidentsOpts()
     *         .from(Instant.now().minus(24, HOURS))
     *         .to(Instant.now()));
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
     * client.stats().summary(new SummaryOpts()
     *         .from(Instant.now().minus(24, HOURS))
     *         .to(Instant.now()));
     * }</pre>
     *
     * @param opts summary query options
     * @return a {@link SummaryModel} containing the aggregated data
     * @throws IOException if the request fails
     */
    public SummaryModel summary(SummaryOpts opts) throws IOException {
        return client.get("/stats/summary", opts.toQueryParams(), SummaryModel.class);
    }
}
