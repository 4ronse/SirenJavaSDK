package dev.ronse.siren.sdk.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.ronse.siren.sdk.clients.options.statistics.*;
import dev.ronse.siren.sdk.clients.options.statistics.enums.DistributionGroupBy;
import dev.ronse.siren.sdk.model.statistics.*;
import dev.ronse.siren.sdk.wrappers.AlertType;

import java.io.IOException;

// TODO: Add no opts method calls
// TODO: Add documentation

public final class StatsClient {
    private static final String PATH = "/stats";

    static StatsClient fromSirenClient(SirenClient client) {
        return new StatsClient(client);
    }

    private final SirenClient client;

    private StatsClient(SirenClient client) {
        this.client = client;
    }

    public CitiesModel cities(CitiesOpts opts) throws IOException {
        return client.get(relativePath("/cities"), opts.toQueryParams(), CitiesModel.class);
    }

    public DistributionModel<AlertType> distributionByCategory(DistributionOpts opts) throws IOException {
        return client.get(
                relativePath("/distribution"),
                opts.toQueryParams(),
                new TypeReference<>() {}
        );
    }

    public DistributionModel<String> distributionByOrigin(DistributionOpts opts) throws IOException {
        return client.get(
                relativePath("/distribution"),
                opts.groupBy(DistributionGroupBy.ORIGIN).toQueryParams(),
                new TypeReference<>() {}
        );
    }

    public HistoryModel history(HistoryOpts opts) throws IOException {
        return client.get(relativePath("/history"), opts.toQueryParams(), HistoryModel.class);
    }

    public IncidentsModel incidents(IncidentsOpts opts) throws IOException {
        return client.get(relativePath("/incidents"), opts.toQueryParams(), IncidentsModel.class);
    }

    public SummaryModel summary(SummaryOpts opts) throws IOException {
        return client.get(relativePath("/summary"), opts.toQueryParams(), SummaryModel.class);
    }

    private String relativePath(String path) {
        path = path.strip();
        return PATH + (path.startsWith("/") ? path : "/" + path);
    }
}
