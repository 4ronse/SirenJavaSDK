package dev.ronse.siren.sdk.clients.options.statistics.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CitiesSort {
    @JsonProperty("count") COUNT,
    @JsonProperty("city")  CITY,
    @JsonProperty("zone")  ZONE
}