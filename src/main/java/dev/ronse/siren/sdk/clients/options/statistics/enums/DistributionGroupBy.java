package dev.ronse.siren.sdk.clients.options.statistics.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DistributionGroupBy {
    @JsonProperty("category") CATEGORY,
    @JsonProperty("origin")   ORIGIN
}
