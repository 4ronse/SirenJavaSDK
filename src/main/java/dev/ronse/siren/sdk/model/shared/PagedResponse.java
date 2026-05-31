package dev.ronse.siren.sdk.model.shared;

import dev.ronse.siren.sdk.model.PaginatedModel;

import java.util.List;

public record PagedResponse<T> (
        List<T> data,
        Pagination pagination
) implements PaginatedModel<T> { }
