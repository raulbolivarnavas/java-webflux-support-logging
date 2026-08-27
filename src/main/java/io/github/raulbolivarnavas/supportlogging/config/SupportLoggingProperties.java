package io.github.raulbolivarnavas.supportlogging.config;

import io.github.raulbolivarnavas.supportlogging.masking.MaskingProperties;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for support logging functionality.
 * This record holds the logging level configuration and provides a method to resolve the effective level.
 *
 * @param level the configured support logging level
 */
@ConfigurationProperties(prefix = "support.logging")
public record SupportLoggingProperties(
        SupportLogLevel level,
        MaskingProperties masking
) {
    /**
     * Resolves the effective logging level, returning a default of INFO if not configured.
     *
     * @return the resolved support logging level
     */
    public SupportLogLevel resolvedLevel() {
        return level != null
                ? level
                : SupportLogLevel.INFO;
    }

    public MaskingProperties resolvedMasking() {
        return masking != null
                ? masking
                : new MaskingProperties(
                true,
                null
        );
    }
}