package io.github.raulbolivarnavas.supportlogging.json;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 3 implementation of {@link JsonFormatter} used by the Spring Boot 4 module.
 */
public class Jackson3JsonFormatter implements JsonFormatter {

    private final JsonMapper jsonMapper;

    /**
     * Creates a formatter backed by the supplied JSON mapper.
     *
     * @param jsonMapper the JSON mapper to use
     */
    public Jackson3JsonFormatter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
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
            return jsonMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
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
            return jsonMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(value);
        } catch (JacksonException exception) {
            return String.valueOf(value);
        }
    }
}
