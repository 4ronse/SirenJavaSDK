package dev.ronse.siren.sdk.wrappers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public final class AlertType {
    public static final AlertType MISSILES                         = new AlertType("missiles");
    public static final AlertType RADIOLOGICAL_EVENT               = new AlertType("radiologicalEvent");
    public static final AlertType EARTHQUAKE                       = new AlertType("earthQuake");
    public static final AlertType TSUNAMI                          = new AlertType("tsunami");
    public static final AlertType HOSTILE_AIRCRAFT_INTRUSION       = new AlertType("hostileAircraftIntrusion");
    public static final AlertType HAZARDOUS_MATERIALS              = new AlertType("hazardousMaterials");
    public static final AlertType TERRORIST_INFILTRATION           = new AlertType("terroristInfiltration");
    public static final AlertType NEWS_FLASH                       = new AlertType("newsFlash");
    public static final AlertType END_ALERT                        = new AlertType("endAlert");

    // Drills
    public static final AlertType MISSILES_DRILL                       = new AlertType("missilesDrill");
    public static final AlertType RADIOLOGICAL_EVENT_DRILL             = new AlertType("radiologicalEventDrill");
    public static final AlertType EARTHQUAKE_DRILL                     = new AlertType("earthQuakeDrill");
    public static final AlertType TSUNAMI_DRILL                        = new AlertType("tsunamiDrill");
    public static final AlertType HOSTILE_AIRCRAFT_INTRUSION_DRILL     = new AlertType("hostileAircraftIntrusionDrill");
    public static final AlertType HAZARDOUS_MATERIALS_DRILL            = new AlertType("hazardousMaterialsDrill");
    public static final AlertType TERRORIST_INFILTRATION_DRILL         = new AlertType("terroristInfiltrationDrill");


    private static final Map<String, AlertType> registry = new HashMap<>();
    static {
        for (Field f : AlertType.class.getDeclaredFields()) {
            if(f.getType() == AlertType.class && Modifier.isStatic(f.getModifiers())) {
                try {
                    AlertType type = (AlertType) f.get(null);
                    registry.put(type.rawValue, type);
                } catch (IllegalAccessException ignored) { }
            }
        }
    }

    public static Collection<AlertType> getKnownTypes() {
        return registry.values();
    }


    private final String rawValue;

    private AlertType(String rawValue) {
        this.rawValue = rawValue;
    }

    @JsonCreator
    public static AlertType fromValue(String value) {
        return registry.getOrDefault(value, new AlertType(value));
    }

    public String getRawValue() {
        return rawValue;
    }

    public boolean isKnown() {
        return registry.containsKey(rawValue);
    }

    public boolean isDrill() {
        return rawValue.endsWith("Drill");
    }

    public String getDisplayName() {
        return Arrays.stream(
                        rawValue.replaceAll("([A-Z])", " $1")
                                .split(" "))
                .filter(s -> !s.isEmpty())
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" ")) + (isKnown() ? "" : " [UNKNOWN]");
    }

    @JsonValue
    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof AlertType)) return false;
        return Objects.equals(rawValue, ((AlertType) o).rawValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rawValue);
    }
}