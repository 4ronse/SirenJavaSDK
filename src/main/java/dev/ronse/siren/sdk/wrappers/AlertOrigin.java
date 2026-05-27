package dev.ronse.siren.sdk.wrappers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class AlertOrigin {
    public static final AlertOrigin GAZA = new AlertOrigin("gaza");
    public static final AlertOrigin LEBANON = new AlertOrigin("lebanon");
    public static final AlertOrigin IRAN = new AlertOrigin("iran");
    public static final AlertOrigin YEMEN = new AlertOrigin("yemen");
    public static final AlertOrigin FA = new AlertOrigin("fa");
    public static final AlertOrigin IRAQ = new AlertOrigin("iraq");
    public static final AlertOrigin SYRIA = new AlertOrigin("syria");
    public static final AlertOrigin ISRAEL = new AlertOrigin("israel"); // Huh

    private static final Map<String, AlertOrigin> registry = new HashMap<>();
    static {
        for (Field f : AlertOrigin.class.getDeclaredFields()) {
            if(f.getType() == AlertOrigin.class && Modifier.isStatic(f.getModifiers())) {
                try {
                    AlertOrigin type = (AlertOrigin) f.get(null);
                    registry.put(type.rawValue, type);
                } catch (IllegalAccessException ignored) { }
            }
        }
    }

    private final String rawValue;

    private AlertOrigin(String rawValue) {
        this.rawValue = rawValue;
    }

    @JsonCreator
    public static AlertOrigin fromValue(String value) {
        return registry.getOrDefault(value, new AlertOrigin(value));
    }

    @JsonValue
    @Override
    public String toString() {
        return rawValue;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof AlertOrigin)) return false;
        return Objects.equals(rawValue, ((AlertOrigin) o).rawValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rawValue);
    }
}
