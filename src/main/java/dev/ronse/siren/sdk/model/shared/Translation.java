package dev.ronse.siren.sdk.model.shared;

import org.jetbrains.annotations.Nullable;

public record Translation(
        @Nullable String ar,
        @Nullable String en,
        @Nullable String es,
        @Nullable String he,
        @Nullable String ru
) { }
