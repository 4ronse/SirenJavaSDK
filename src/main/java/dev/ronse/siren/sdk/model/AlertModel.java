package dev.ronse.siren.sdk.model;

import dev.ronse.siren.sdk.wrappers.AlertType;

import java.time.Instant;
import java.util.List;

public record AlertModel (
        AlertType type,
        List<String> cities,
        String title,
        String instructions,
        Instant timestamp,
        boolean isTest
) { }
