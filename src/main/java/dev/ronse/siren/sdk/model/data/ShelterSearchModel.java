package dev.ronse.siren.sdk.model.data;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record ShelterSearchModel (
        boolean success,
        int count,
        List<ShelterResult> results
) {
    public record ShelterResult (
            UUID id,
            String address,
            String city,
            @Nullable String building_name,
            double lat,
            double lon,
            int distance_meters,
            double distance_kilometers,
            @Nullable Integer capacity,
            boolean wheelchair_accessible,
            boolean has_stairs,
            String shelter_type,
            String shelter_type_he,
            boolean is_official,
            String notes
    ) { }
}
