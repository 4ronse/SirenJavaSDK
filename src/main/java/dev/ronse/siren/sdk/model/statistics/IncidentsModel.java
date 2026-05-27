package dev.ronse.siren.sdk.model.statistics;

import dev.ronse.siren.sdk.wrappers.AlertType;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public record IncidentsModel (
        IncidentsSummary summary,
        @Nullable List<IncidentData> data
) {

    public record IncidentsSummary (
            int totalNewsFlash,
            int withRealAlerts,
            int withoutRealAlerts,
            double realAlertRate
    ) { }

    public record IncidentData (
            String waveId,
            Instant waveTime,
            @Nullable Instant waveEnd,
            boolean hadRealAlert,
            @Nullable AlertType realAlertType,
            @Nullable Instant realAlertTime,
            boolean timedOut
    ) { }

}
