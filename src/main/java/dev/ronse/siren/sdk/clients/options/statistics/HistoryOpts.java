package dev.ronse.siren.sdk.clients.options.statistics;

import dev.ronse.siren.sdk.clients.options.SirenOpts;
import dev.ronse.siren.sdk.clients.options.statistics.enums.DataInclude;
import dev.ronse.siren.sdk.clients.options.statistics.enums.HistorySort;
import dev.ronse.siren.sdk.clients.options.statistics.enums.SortOrder;
import dev.ronse.siren.sdk.utils.QueryParametersList;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public final class HistoryOpts implements SirenOpts {
    private Instant startDate;
    private Instant endDate;
    private Integer limit;
    private Integer offset;
    private Integer cityId;
    private String cityName;
    private String search;
    private String category;
    private List<String> origin;
    private HistorySort sort;
    private SortOrder order;
    private List<DataInclude> include;

    public HistoryOpts startDate(Instant v)       { this.startDate = v;       return this; }
    public HistoryOpts endDate(Instant v)         { this.endDate = v;         return this; }
    public HistoryOpts limit(int v)               { this.limit = v;           return this; }
    public HistoryOpts offset(int v)              { this.offset = v;          return this; }
    public HistoryOpts cityId(int v)              { this.cityId = v;          return this; }
    public HistoryOpts cityName(String v)         { this.cityName = v;        return this; }
    public HistoryOpts search(String v)           { this.search = v;          return this; }
    public HistoryOpts category(String v)         { this.category = v;        return this; }
    public HistoryOpts origin(String... v)        { this.origin = List.of(v); return this; }
    public HistoryOpts sort(HistorySort v)        { this.sort = v;            return this; }
    public HistoryOpts order(SortOrder v)         { this.order = v;           return this; }
    public HistoryOpts include(DataInclude... v)  { this.include = List.of(v); return this; }

    @Override
    public QueryParametersList toQueryParams() {
        QueryParametersList p = new QueryParametersList();
        if (startDate != null) p.set("startDate", startDate.toString());
        if (endDate   != null) p.set("endDate",   endDate.toString());
        if (limit     != null) p.set("limit",     limit);
        if (offset    != null) p.set("offset",    offset);
        if (cityId    != null) p.set("cityId",    cityId);
        if (cityName  != null) p.set("cityName",  cityName);
        if (search    != null) p.set("search",    search);
        if (category  != null) p.set("category",  category);
        if (origin    != null) p.set("origin",    String.join(",", origin));
        if (sort      != null) p.set("sort",      sort.name().toLowerCase());
        if (order     != null) p.set("order",     order.name().toLowerCase());
        if (include   != null) p.set("include",   include.stream().map(DataInclude::getValue).collect(Collectors.joining(",")));
        return p;
    }
}