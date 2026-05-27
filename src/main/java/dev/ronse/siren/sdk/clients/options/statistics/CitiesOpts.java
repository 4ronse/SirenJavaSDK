package dev.ronse.siren.sdk.clients.options.statistics;

import dev.ronse.siren.sdk.clients.options.SirenOpts;
import dev.ronse.siren.sdk.clients.options.statistics.enums.CitiesSort;
import dev.ronse.siren.sdk.clients.options.statistics.enums.DataInclude;
import dev.ronse.siren.sdk.clients.options.statistics.enums.SortOrder;
import dev.ronse.siren.sdk.utils.QueryParametersList;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public final class CitiesOpts implements SirenOpts {
    private Instant startDate;
    private Instant endDate;
    private Integer limit;
    private Integer offset;
    private List<String> origin;
    private String search;
    private String zone;
    private CitiesSort sort;
    private SortOrder order;
    private List<DataInclude> include;

    public CitiesOpts startDate(Instant v)        { this.startDate = v;       return this; }
    public CitiesOpts endDate(Instant v)          { this.endDate = v;         return this; }
    public CitiesOpts limit(int v)                { this.limit = v;           return this; }
    public CitiesOpts offset(int v)               { this.offset = v;          return this; }
    public CitiesOpts origin(String... v)         { this.origin = List.of(v); return this; }
    public CitiesOpts search(String v)            { this.search = v;          return this; }
    public CitiesOpts zone(String v)              { this.zone = v;            return this; }
    public CitiesOpts sort(CitiesSort v)          { this.sort = v;            return this; }
    public CitiesOpts order(SortOrder v)          { this.order = v;           return this; }
    public CitiesOpts include(DataInclude... v)   { this.include = List.of(v); return this; }

    @Override
    public QueryParametersList toQueryParams() {
        QueryParametersList p = new QueryParametersList();
        if (startDate != null) p.set("startDate", startDate.toString());
        if (endDate   != null) p.set("endDate",   endDate.toString());
        if (limit     != null) p.set("limit",     limit);
        if (offset    != null) p.set("offset",    offset);
        if (origin    != null) p.set("origin",    String.join(",", origin));
        if (search    != null) p.set("search",    search);
        if (zone      != null) p.set("zone",      zone);
        if (sort      != null) p.set("sort",      sort.name().toLowerCase());
        if (order     != null) p.set("order",     order.name().toLowerCase());
        if (include   != null) p.set("include",   include.stream().map(DataInclude::getValue).collect(Collectors.joining(",")));
        return p;
    }
}
