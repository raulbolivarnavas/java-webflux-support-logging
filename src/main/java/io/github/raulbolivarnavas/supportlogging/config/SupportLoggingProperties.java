package io.github.raulbolivarnavas.supportlogging.config;

import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for support logging functionality.
 * This record holds the logging level configuration and provides a method to resolve the effective level.
 *
 * @param support the configured support logging level
 */
@ConfigurationProperties(prefix = "ficohsa.logging.level")
public record SupportLoggingProperties(SupportLogLevel support) {
    /**
     * Resolves the effective logging level, returning a default of INFO if not configured.
     *
     * @return the resolved support logging level
     */
    public SupportLogLevel resolvedLevel() {
        return support != null ? support : SupportLogLevel.INFO;
    }
}