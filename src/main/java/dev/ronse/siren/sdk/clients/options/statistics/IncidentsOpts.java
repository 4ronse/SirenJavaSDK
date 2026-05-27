package dev.ronse.siren.sdk.clients.options.statistics;

import dev.ronse.siren.sdk.clients.options.SirenOpts;
import dev.ronse.siren.sdk.clients.options.statistics.enums.SortOrder;
import dev.ronse.siren.sdk.utils.QueryParametersList;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

public final class IncidentsOpts implements SirenOpts {
    private final String city;  // required
    private Instant startDate;
    private Instant endDate;
    private List<String> origin;
    private Integer timeoutMinutes;
    private String sort;
    private SortOrder order;

    public IncidentsOpts(@NotNull String city) {
        if (city.isBlank()) throw new IllegalArgumentException("city is required");
        this.city = city;
    }

    public IncidentsOpts startDate(Instant v)      { this.startDate = v;       return this; }
    public IncidentsOpts endDate(Instant v)        { this.endDate = v;         return this; }
    public IncidentsOpts origin(String... v)       { this.origin = List.of(v); return this; }
    public IncidentsOpts timeoutMinutes(int v)     { this.timeoutMinutes = v;  return this; }
    public IncidentsOpts sort(String v)            { this.sort = v;            return this; }
    public IncidentsOpts order(SortOrder v)        { this.order = v;           return this; }

    @Override
    public QueryParametersList toQueryParams() {
        QueryParametersList p = new QueryParametersList();
        p.set("city", city);  // always added — required
        if (startDate      != null) p.set("startDate",      startDate.toString());
        if (endDate        != null) p.set("endDate",        endDate.toString());
        if (origin         != null) p.set("origin",         String.join(",", origin));
        if (timeoutMinutes != null) p.set("timeoutMinutes", timeoutMinutes);
        if (sort           != null) p.set("sort",           sort);
        if (order          != null) p.set("order",          order.name().toLowerCase());
        return p;
    }
}