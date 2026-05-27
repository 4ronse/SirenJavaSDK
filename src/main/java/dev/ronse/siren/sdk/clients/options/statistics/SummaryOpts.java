package dev.ronse.siren.sdk.clients.options.statistics;

import dev.ronse.siren.sdk.clients.options.SirenOpts;
import dev.ronse.siren.sdk.clients.options.statistics.enums.SummaryInclude;
import dev.ronse.siren.sdk.clients.options.statistics.enums.TimelineGroup;
import dev.ronse.siren.sdk.utils.QueryParametersList;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public final class SummaryOpts implements SirenOpts {
    private Instant startDate;
    private Instant endDate;
    private List<String> origin;
    private List<SummaryInclude> include;
    private Integer topLimit;
    private TimelineGroup timelineGroup;

    public SummaryOpts startDate(Instant v)          { this.startDate = v;     return this; }
    public SummaryOpts endDate(Instant v)            { this.endDate = v;       return this; }
    public SummaryOpts origin(String... v)           { this.origin = List.of(v); return this; }
    public SummaryOpts include(SummaryInclude... v)  { this.include = List.of(v); return this; }
    public SummaryOpts topLimit(int v)               { this.topLimit = v;      return this; }
    public SummaryOpts timelineGroup(TimelineGroup v){ this.timelineGroup = v; return this; }

    @Override
    public QueryParametersList toQueryParams() {
        QueryParametersList p = new QueryParametersList();
        if (startDate     != null) p.set("startDate",     startDate.toString());
        if (endDate       != null) p.set("endDate",       endDate.toString());
        if (origin        != null) p.set("origin",        String.join(",", origin));
        if (include       != null) p.set("include",       include.stream().map(SummaryInclude::getValue).collect(Collectors.joining(",")));
        if (topLimit      != null) p.set("topLimit",      topLimit);
        if (timelineGroup != null) p.set("timelineGroup", timelineGroup.name().toLowerCase());
        return p;
    }
}
