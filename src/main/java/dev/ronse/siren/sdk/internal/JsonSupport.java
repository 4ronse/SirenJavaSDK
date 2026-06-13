package dev.ronse.siren.sdk.internal;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public final class JsonSupport {
    private static final ObjectMapper objectMapper = createObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private static ObjectMapper createObjectMapper() {
        SimpleModule instantDeserializerModule = new SimpleModule();
        instantDeserializerModule.addDeserializer(Instant.class, new FlexibleInstantDeserializer());

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(instantDeserializerModule)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
    }

    private static final class FlexibleInstantDeserializer extends JsonDeserializer<Instant> {
        private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .optionalStart().appendLiteral('T').optionalEnd()
                .optionalStart().appendLiteral(' ').optionalEnd()
                .appendPattern("HH:mm:ss")
                .optionalStart()
                .appendFraction(
                        ChronoField.MILLI_OF_SECOND,
                        0,
                        3,
                        true
                )
                .optionalEnd()
                .appendPattern("[XXXXX][XXXX][XXX][XX][X]")
                .toFormatter()
                .withZone(ZoneOffset.UTC);

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext _ctx) throws IOException, JacksonException {
            return Instant.from(FORMATTER.parse(p.getText()));
        }
    }
}
