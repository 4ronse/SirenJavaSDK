package dev.ronse.siren.sdk.clients.options.statistics.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum HistorySort {
    @JsonProperty("receivedAt") TIMESTAMP,
    @JsonProperty("type")      TYPE,
    @JsonProperty("origin")    ORIGIN
}