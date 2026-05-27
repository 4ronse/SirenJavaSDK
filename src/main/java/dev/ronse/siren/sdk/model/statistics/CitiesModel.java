package dev.ronse.siren.sdk.model.statistics;

import dev.ronse.siren.sdk.model.shared.Pagination;
import dev.ronse.siren.sdk.model.shared.Translation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// https://siren.co.il/docs/stats/cities
public record CitiesModel (
        List<CitiesData> data,
        Pagination pagination
) {

    public record CitiesData(
            String city,
            String cityZone,
            int count,

            // Optional fields - null when not requested
            @Nullable Translations translations,
            @Nullable Double lat,
            @Nullable Double lng,
            @Nullable List<Double[]> polygons
    ) { }

    public record Translations (
            Translation name,
            Translation zone
    ) { }

}
