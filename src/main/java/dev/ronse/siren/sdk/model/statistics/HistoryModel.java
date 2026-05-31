package dev.ronse.siren.sdk.model.statistics;

import dev.ronse.siren.sdk.wrappers.AlertOrigin;
import dev.ronse.siren.sdk.wrappers.AlertType;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// https://siren.co.il/docs/stats/history
public record HistoryModel (
        UUID id,
        Instant timestamp,
        AlertType type,
        @Nullable AlertOrigin origin,
        List<HistoryCity> cities
) {

    public record HistoryCity (
            int id,
            String name,
            String zone,
            @Nullable Double lat,
            @Nullable Double lng,
            @Nullable CitiesModel.Translations translations,
            @Nullable List<double[]> polygon
    ) { }
}
