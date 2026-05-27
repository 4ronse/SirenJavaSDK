package dev.ronse.siren.sdk.clients.options.data;

import dev.ronse.siren.sdk.clients.options.data.enums.CityInclude;
import dev.ronse.siren.sdk.utils.QueryParametersList;

import java.util.List;
import java.util.stream.Collectors;

public final class CitiesCatalogOpts {
    private String search;
    private String zone;
    private Integer limit;
    private Integer offset;
    private List<CityInclude> include;

    public CitiesCatalogOpts search(String v)          { this.search = v;          return this; }
    public CitiesCatalogOpts zone(String v)            { this.zone = v;            return this; }
    public CitiesCatalogOpts limit(int v)              { this.limit = v;           return this; }
    public CitiesCatalogOpts offset(int v)             { this.offset = v;          return this; }
    public CitiesCatalogOpts include(CityInclude... v) { this.include = List.of(v); return this; }

    public QueryParametersList toQueryParams() {
        QueryParametersList p = new QueryParametersList();
        if (search  != null) p.set("search",  search);
        if (zone    != null) p.set("zone",    zone);
        if (limit   != null) p.set("limit",   limit);
        if (offset  != null) p.set("offset",  offset);
        if (include != null) p.set("include", include.stream()
                .map(CityInclude::getValue)
                .collect(Collectors.joining(",")));
        return p;
    }
}
