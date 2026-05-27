package dev.ronse.siren.sdk.clients.options.statistics;

import dev.ronse.siren.sdk.clients.options.SirenOpts;
import dev.ronse.siren.sdk.clients.options.statistics.enums.DistributionGroupBy;
import dev.ronse.siren.sdk.clients.options.statistics.enums.DistributionSort;
import dev.ronse.siren.sdk.clients.options.statistics.enums.SortOrder;
import dev.ronse.siren.sdk.utils.QueryParametersList;

import java.time.Instant;
import java.util.List;

public final class DistributionOpts implements SirenOpts {
    private Instant startDate;
    private Instant endDate;
    private List<String> origin;
    private DistributionGroupBy groupBy;
    private String category;
    private Integer limit;
    private Integer offset;
    private DistributionSort sort;
    private SortOrder order;

    public DistributionOpts startDate(Instant v)          { this.startDate = v;       return this; }
    public DistributionOpts endDate(Instant v)            { this.endDate = v;         return this; }
    public DistributionOpts origin(String... v)           { this.origin = List.of(v); return this; }
    public DistributionOpts groupBy(DistributionGroupBy v){ this.groupBy = v;         return this; }
    public DistributionOpts category(String v)            { this.category = v;        return this; }
    public DistributionOpts limit(int v)                  { this.limit = v;           return this; }
    public DistributionOpts offset(int v)                 { this.offset = v;          return this; }
    public DistributionOpts sort(DistributionSort v)      { this.sort = v;            return this; }
    public DistributionOpts order(SortOrder v)            { this.order = v;           return this; }

    @Override
    public QueryParametersList toQueryParams() {
        QueryParametersList p = new QueryParametersList();
        if (startDate != null) p.set("startDate", startDate.toString());
        if (endDate   != null) p.set("endDate",   endDate.toString());
        if (origin    != null) p.set("origin",    String.join(",", origin));
        if (groupBy   != null) p.set("groupBy",   groupBy.name().toLowerCase());
        if (category  != null) p.set("category",  category);
        if (limit     != null) p.set("limit",     limit);
        if (offset    != null) p.set("offset",    offset);
        if (sort      != null) p.set("sort",      sort.name().toLowerCase());
        if (order     != null) p.set("order",     order.name().toLowerCase());
        return p;
    }
}