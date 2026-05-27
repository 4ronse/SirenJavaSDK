package dev.ronse.siren.sdk.clients.options.statistics.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TimelineGroup {
    @JsonProperty("hour")  HOUR,
    @JsonProperty("day")   DAY,
    @JsonProperty("week")  WEEK,
    @JsonProperty("month") MONTH
}
