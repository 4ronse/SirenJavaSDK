package dev.ronse.siren.sdk.clients.options.statistics.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DistributionSort {
    @JsonProperty("count") COUNT,
    @JsonProperty("label") LABEL
}