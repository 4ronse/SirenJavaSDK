package dev.ronse.siren.sdk.model.statistics;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record SummaryModel (
        Totals totals,
        int uniqueCities,
        int uniqueZones,
        int uniqueOrigins,

        // Optional fields - null when not requested
        @Nullable List<CityCount> topCities,
        @Nullable List<ZoneCount> topZones,
        @Nullable List<PeriodCount> timeline,
        @Nullable Peak peak,
        @Nullable List<OriginCount> topOrigins
) {

    public record Totals(
            long range,
            long last24h,
            long last7d,
            long last30d
    ) {}

    public record CityCount(
            String city,
            String zone,
            long count
    ) {}

    public record ZoneCount(
            String zone,
            long count
    ) {}

    public record PeriodCount(
            String period,
            long count
    ) {}

    public record Peak(
            String period,
            long count
    ) {}

    public record OriginCount(
            String origin,
            long count
    ) {}

}
