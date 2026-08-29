package io.github.raulbolivarnavas.supportlogging.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson 2 implementation of {@link JsonFormatter} used by the Spring Boot 3 module.
 */
public class Jackson2JsonFormatter implements JsonFormatter {

    private final ObjectMapper objectMapper;

    /**
     * Creates a formatter backed by the supplied object mapper.
     *
     * @param objectMapper the object mapper to use
     */
    public Jackson2JsonFormatter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Serializes a value to a compact JSON string when possible.
     *
     * @param value the value to serialize
     * @return the compact JSON string, the raw string, or {@code String.valueOf(value)}
     */
    @Override
    public String compact(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String text) {
            return text;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return String.valueOf(value);
        }
    }

    /**
     * Serializes a value to a pretty-printed JSON string when possible.
     *
     * @param value the value to serialize
     * @return the pretty-printed JSON string, the raw string, or {@code String.valueOf(value)}
     */
    @Override
    public String pretty(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String text) {
            return text;
        }

        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return String.valueOf(value);
        }
    }
}
