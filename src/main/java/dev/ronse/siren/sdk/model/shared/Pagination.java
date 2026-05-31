package dev.ronse.siren.sdk.model.shared;

public record Pagination (
        int total,
        int limit,
        int offset,
        boolean hasMore
) {

    // Convenience
    // May not be needed later idk
    public int pageNumber() {
        return limit == 0 ? 1 : (offset / limit) + 1;
    }

}
