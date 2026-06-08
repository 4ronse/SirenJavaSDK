package dev.ronse.siren.sdk.utils;

public class StringUtils {
    private static final int TRUNCATE_MAX_LEN = 1000;

    public static String truncate(String text) {
        if (text == null || text.isBlank()) return "";
        if (text.length() <= TRUNCATE_MAX_LEN) return text;
        return text.substring(0, TRUNCATE_MAX_LEN) + "...";
    }
}
