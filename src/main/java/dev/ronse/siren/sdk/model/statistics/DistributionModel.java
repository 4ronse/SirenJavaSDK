package dev.ronse.siren.sdk.model.statistics;

import dev.ronse.siren.sdk.model.shared.Pagination;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// https://api.siren.co.il/stats/distribution
public record DistributionModel<T> (
        List<DistributionData<T>> data,
        int totalAlerts,
        Pagination pagination
) {

    public record DistributionData<T> (
            @Nullable T label,
            int count
    ) { }

}
