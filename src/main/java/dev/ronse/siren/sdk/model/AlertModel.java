package dev.ronse.siren.sdk.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import dev.ronse.siren.sdk.wrappers.AlertType;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public record AlertModel(
        AlertType type,
        List<String> cities,
        String title,
        String instructions,
        @Nullable @JsonAlias({"timestamp", "receivedAt"}) Instant receivedAt,
        boolean isTest
) {
    /** Returns a copy of this alert with {@code isTest} forced to {@code true}. */
    public AlertModel asTest() {
        return new AlertModel(type, cities, title, instructions, receivedAt, true);
    }
}