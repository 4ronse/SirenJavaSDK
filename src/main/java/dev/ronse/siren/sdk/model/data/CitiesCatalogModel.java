package dev.ronse.siren.sdk.model.data;

import dev.ronse.siren.sdk.model.shared.Pagination;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record CitiesCatalogModel(
        List<CityEntry> data,
        Pagination pagination
) {
    public record CityEntry(
            int id,
            String name,
            @Nullable String zone,
            @Nullable Map<String, String> translations,
            @Nullable Double lat,
            @Nullable Double lng,
            @Nullable Integer countdown
    ) { }
}