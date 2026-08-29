package io.github.raulbolivarnavas.supportlogging.config;

import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;

/**
 * Minimal configuration contract shared by the core logger and the Spring Boot bindings.
 */
public interface SupportLoggingConfiguration {
    /**
     * Resolves the effective log level to use for the current environment.
     *
     * @return the effective support log level
     */
    SupportLogLevel resolvedLevel();
}
