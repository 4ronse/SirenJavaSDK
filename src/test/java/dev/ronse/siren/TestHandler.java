package dev.ronse.siren;

import dev.ronse.siren.sdk.clients.interfaces.OnAlert;
import dev.ronse.siren.sdk.model.AlertModel;
import dev.ronse.siren.sdk.wrappers.AlertType;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TestHandler {

    private TestHandler() { }

    private static final int MAX_CITIES = 5;
    private static final ZoneId TZ = ZoneId.of("Asia/Jerusalem");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss z");

    // ── ANSI ──────────────────────────────────────────────────────────────────
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String GRAY = "\u001B[90m";
    private static final String WHITE = "\u001B[37m";

    // ── Styles ─────────────────────────────────────────────────────────────────
    private record AlertStyle(String color, String emoji) {
        String apply(String s) {
            return color + s + RESET;
        }

        String bold(String s) {
            return color + BOLD + s + RESET;
        }
    }

    private static final AlertStyle DEFAULT_STYLE = new AlertStyle(WHITE, "⚠️ ");

    private static final Map<AlertType, AlertStyle> STYLES;

    static {
        STYLES = Map.ofEntries(
                Map.entry(AlertType.MISSILES, new AlertStyle(RED, "🚀 ")),
                Map.entry(AlertType.RADIOLOGICAL_EVENT, new AlertStyle(MAGENTA, "☢️  ")),
                Map.entry(AlertType.EARTHQUAKE, new AlertStyle(YELLOW, "🌍 ")),
                Map.entry(AlertType.TSUNAMI, new AlertStyle(CYAN, "🌊 ")),
                Map.entry(AlertType.HOSTILE_AIRCRAFT_INTRUSION, new AlertStyle(RED, "✈️  ")),
                Map.entry(AlertType.HAZARDOUS_MATERIALS, new AlertStyle(YELLOW, "☣️  ")),
                Map.entry(AlertType.TERRORIST_INFILTRATION, new AlertStyle(RED, "🔴 ")),
                Map.entry(AlertType.NEWS_FLASH, new AlertStyle(CYAN, "📢 ")),
                Map.entry(AlertType.END_ALERT, new AlertStyle(GREEN, "✅ ")),
                Map.entry(AlertType.MISSILES_DRILL, new AlertStyle(GRAY, "🚀 ")),
                Map.entry(AlertType.RADIOLOGICAL_EVENT_DRILL, new AlertStyle(GRAY, "☢️  ")),
                Map.entry(AlertType.EARTHQUAKE_DRILL, new AlertStyle(GRAY, "🌍 ")),
                Map.entry(AlertType.TSUNAMI_DRILL, new AlertStyle(GRAY, "🌊 ")),
                Map.entry(AlertType.HOSTILE_AIRCRAFT_INTRUSION_DRILL, new AlertStyle(GRAY, "✈️  ")),
                Map.entry(AlertType.HAZARDOUS_MATERIALS_DRILL, new AlertStyle(GRAY, "☣️  ")),
                Map.entry(AlertType.TERRORIST_INFILTRATION_DRILL, new AlertStyle(GRAY, "🔴 "))
        );
    }

    // ── Handler ────────────────────────────────────────────────────────────────
    @OnAlert
    static void onAlert(AlertModel model) {
        AlertStyle style = STYLES.getOrDefault(model.type(), DEFAULT_STYLE);
        boolean isDrill = model.type().isDrill() || model.isTest();

        String time = model.receivedAt() != null
                ? ZonedDateTime.ofInstant(model.receivedAt(), TZ).format(TIME_FMT)
                : DIM + "-" + RESET;
        List<String> cities = model.cities();
        String citiesStr = cities.stream().limit(MAX_CITIES).collect(Collectors.joining(", "));
        if (cities.size() > MAX_CITIES)
            citiesStr += " (+" + (cities.size() - MAX_CITIES) + " more)";

        // ── Content lines ─────────────────────────────────────────────────────
        String drillTag = isDrill ? DIM + " [DRILL/TEST]" + RESET : "";
        String titleLine = style.bold(style.emoji + model.title()) + drillTag;
        String typeLine = style.apply("  type    ") + model.type();
        String timeLine = style.apply("  time    ") + time;
        String citiesLine = style.apply("  cities  ") + "(" + cities.size() + ")  " + citiesStr;
        boolean hasInstr = model.instructions() != null && !model.instructions().isBlank();
        String instrLine = DIM + "  " + model.instructions() + RESET;

        // ── Auto-size box to widest visible line ──────────────────────────────
        List<String> measured = hasInstr
                ? List.of(titleLine, typeLine, timeLine, citiesLine, instrLine)
                : List.of(titleLine, typeLine, timeLine, citiesLine);

        int inner = measured.stream().mapToInt(TestHandler::visLen).max().orElse(40) + 2;

        // ── Box pieces ────────────────────────────────────────────────────────
        String top = style.apply("╔" + "═".repeat(inner + 2) + "╗");
        String mid = style.apply("╠" + "═".repeat(inner + 2) + "╣");
        String thin = DIM + "╟" + "─".repeat(inner + 2) + "╢" + RESET;
        String bottom = style.apply("╚" + "═".repeat(inner + 2) + "╝");

        // ── Render ────────────────────────────────────────────────────────────
        StringBuilder sb = new StringBuilder("\n");
        sb.append(top).append('\n');
        sb.append(row(titleLine, inner)).append('\n');
        sb.append(mid).append('\n');
        sb.append(row(typeLine, inner)).append('\n');
        sb.append(row(timeLine, inner)).append('\n');
        sb.append(mid).append('\n');
        sb.append(row(citiesLine, inner)).append('\n');
        if (hasInstr) {
            sb.append(thin).append('\n');
            sb.append(row(instrLine, inner)).append('\n');
        }
        sb.append(bottom).append('\n');

        System.out.print(sb);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Wraps a content line in box-drawing borders, padding to {@code innerWidth}.
     */
    private static String row(String content, int innerWidth) {
        int pad = innerWidth - visLen(content);
        return "║ " + content + " ".repeat(Math.max(0, pad)) + " ║";
    }

    private static final Pattern ANSI = Pattern.compile("\u001B\\[[\\d;]*m");

    /**
     * Visible character length, ignoring ANSI escape sequences.
     */
    private static int visLen(String s) {
        return ANSI.matcher(s).replaceAll("").length();
    }
}