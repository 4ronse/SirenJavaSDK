package dev.ronse.siren.sdk.model.shared;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A language-keyed map of translated strings.
 * <p>
 * Convenience accessors are provided for well-known languages ({@link #ar()}, {@link #en()},
 * {@link #es()}, {@link #he()}, {@link #ru()}). Use {@link #get(String)} for any language key
 * the API adds in the future, and {@link #all()} to iterate over everything.
 *
 * <pre>{@code
 * Translation t = city.translations().name();
 * String english  = t.en();               // known language — typed accessor
 * String ukrainian = t.get("uk");         // unknown language — generic accessor
 * t.all().forEach((lang, val) -> ...);    // iterate all
 * }</pre>
 */
public final class Translation {

    private final Map<String, String> languages = new HashMap<>();

    /**
     * Jackson entry point — captures every key in the JSON object, including languages
     * not yet known at compile time.
     */
    @JsonAnySetter
    public void set(String lang, String value) {
        languages.put(lang, value);
    }

    /**
     * Returns the translation for the given language code, or {@code null} if absent.
     * Use this for languages not covered by the named accessors.
     *
     * @param lang BCP-47 language tag, e.g. {@code "uk"}, {@code "fr"}
     */
    @Nullable
    public String get(String lang) {
        return languages.get(lang);
    }

    /** Arabic translation, or {@code null} if not present. */
    @Nullable public String ar() { return get("ar"); }

    /** English translation, or {@code null} if not present. */
    @Nullable public String en() { return get("en"); }

    /** Spanish translation, or {@code null} if not present. */
    @Nullable public String es() { return get("es"); }

    /** Hebrew translation, or {@code null} if not present. */
    @Nullable public String he() { return get("he"); }

    /** Russian translation, or {@code null} if not present. */
    @Nullable public String ru() { return get("ru"); }

    /**
     * All language translations returned by the API for this field.
     * The returned map is unmodifiable.
     */
    public Map<String, String> all() {
        return Collections.unmodifiableMap(languages);
    }

    @Override
    public String toString() {
        return languages.toString();
    }
}