package dev.ronse.siren.sdk.clients.options.statistics.enums;

public enum DataInclude {
    TRANSLATIONS("translations"),
    COORDS("coords"),
    POLYGONS("polygons");

    private final String value;
    DataInclude(String value) { this.value = value; }
    public String getValue() { return value; }
}