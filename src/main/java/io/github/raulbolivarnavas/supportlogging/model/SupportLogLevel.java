package io.github.raulbolivarnavas.supportlogging.model;

/**
 * Enumeration of logging levels for support logging functionality.
 * Determines the verbosity and detail of the support log output.
 */
public enum SupportLogLevel {
    /**
     * Info level: Logs support operations with compact JSON representation.
     */
    INFO,
    /**
     * Debug level: Logs detailed support operations including cURL command representation.
     */
    DEBUG
}
