package dev.ronse.siren.sdk.clients.options.data;

import dev.ronse.siren.sdk.utils.QueryParametersList;

public final class ShelterSearchOpts {
    private final double lat;  // required
    private final double lon;  // required
    private Integer limit;
    private Double radiusKm;
    private Boolean wheelchairOnly;
    private String shelterType;
    private String city;

    public ShelterSearchOpts(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public ShelterSearchOpts limit(int v)             { this.limit = v;           return this; }
    public ShelterSearchOpts radiusKm(double v)       { this.radiusKm = v;        return this; }
    public ShelterSearchOpts wheelchairOnly(boolean v){ this.wheelchairOnly = v;  return this; }
    public ShelterSearchOpts shelterType(String v)    { this.shelterType = v;     return this; }
    public ShelterSearchOpts city(String v)           { this.city = v;            return this; }

    public QueryParametersList toQueryParams() {
        QueryParametersList p = new QueryParametersList();
        p.set("lat", lat);
        p.set("lon", lon);
        if (limit         != null) p.set("limit",         limit);
        if (radiusKm      != null) p.set("radiusKm",      radiusKm);
        if (wheelchairOnly!= null) p.set("wheelchairOnly",wheelchairOnly);
        if (shelterType   != null) p.set("shelterType",   shelterType);
        if (city          != null) p.set("city",          city);
        return p;
    }
}