package dev.ronse.siren.sdk.model.shared;

public record Pagination (
        int total,
        int limit,
        int offset,
        boolean hasMore
) { }
