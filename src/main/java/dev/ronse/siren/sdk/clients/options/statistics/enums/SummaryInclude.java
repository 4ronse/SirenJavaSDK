package dev.ronse.siren.sdk.clients.options.statistics.enums;


public enum SummaryInclude {
    TOP_CITIES("topCities"),
    TOP_ZONES("topZones"),
    TOP_ORIGINS("topOrigins"),
    TIMELINE("timeline"),
    PEAK("peak");

    private final String value;
    SummaryInclude(String value) { this.value = value; }
    public String getValue() { return value; }
}