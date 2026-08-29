package io.github.raulbolivarnavas.supportlogging.json;

/**
 * Formats arbitrary values for compact or human-readable log output.
 */
public interface JsonFormatter {
    /**
     * Serializes a value in the smallest readable representation available.
     *
     * @param value the value to serialize
     * @return a compact representation of the value
     */
    String compact(Object value);

    /**
     * Serializes a value using a pretty-printed representation.
     *
     * @param value the value to serialize
     * @return a pretty-printed representation of the value
     */
    String pretty(Object value);
}
