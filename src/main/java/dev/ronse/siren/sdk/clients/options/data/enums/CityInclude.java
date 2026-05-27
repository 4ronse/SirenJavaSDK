package dev.ronse.siren.sdk.clients.options.data.enums;

public enum CityInclude {
    TRANSLATIONS("translations"),
    COORDS("coords"),
    COUNTDOWN("countdown"),
    POLYGONS("polygons");

    private final String value;
    CityInclude(String value) { this.value = value; }
    public String getValue() { return value; }
}
