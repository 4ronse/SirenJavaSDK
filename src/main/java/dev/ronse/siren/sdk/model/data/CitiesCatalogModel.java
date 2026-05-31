package dev.ronse.siren.sdk.model.data;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record CitiesCatalogModel(
        int id,
        String name,
        @Nullable String zone,
        @Nullable Map<String, String> translations,
        @Nullable Double lat,
        @Nullable Double lng,
        @Nullable Integer countdown
) { }