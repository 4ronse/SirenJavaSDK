package dev.ronse.siren.sdk.utils;

import okhttp3.HttpUrl;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class QueryParametersList {
    public static final QueryParametersList EMPTY = new QueryParametersList(true);

    private final Map<String, String> map = new HashMap<>();
    private final boolean immutable;

    public QueryParametersList() {
        immutable = false;
    }

    private QueryParametersList(boolean immutable) {
        this.immutable = immutable;
    }

    public QueryParametersList set(String k, String v) {
        if (immutable) throw new UnsupportedOperationException("EMPTY query list is immutable");

        map.put(k, v);
        return this;
    }

    public <T> QueryParametersList set(String k, T v) {
        return set(k, String.valueOf(v));
    }

    public QueryParametersList set(String k, Enum<?> v) {
        return set(k, v.toString());
    }

    public QueryParametersList unset(String k) {
        if (immutable) throw new UnsupportedOperationException("EMPTY query list is immutable");

        map.remove(k);
        return this;
    }

    public HttpUrl.Builder addToHttpUrlBuilder(HttpUrl.Builder builder) {
        for(Map.Entry<String, String> param : map.entrySet()) {
            String value = param.getValue();
            value = value == null ? "" : value;
            builder.addQueryParameter(param.getKey(), value);
        }

        return builder;
    }

    public String toQueryString() {
        return map.entrySet()
                .stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
