package dev.ronse.siren.sdk.model;

import dev.ronse.siren.sdk.model.shared.Pagination;

import java.util.List;

public interface PaginatedModel <T> {

    List<T> data();
    Pagination pagination();

}
