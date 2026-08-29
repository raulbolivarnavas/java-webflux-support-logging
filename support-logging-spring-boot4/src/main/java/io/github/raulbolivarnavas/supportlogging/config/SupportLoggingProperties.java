package io.github.raulbolivarnavas.supportlogging.config;

import io.github.raulbolivarnavas.supportlogging.masking.MaskingProperties;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the Spring Boot 4 support-logging configuration properties.
 */
@ConfigurationProperties(prefix = "support.logging")
public record SupportLoggingProperties(
        SupportLogLevel level,
        MaskingProperties masking
) implements SupportLoggingConfiguration {

    /**
     * Resolves the effective log level.
     *
     * @return the configured level, or {@link SupportLogLevel#INFO} when absent
     */
    @Override
    public SupportLogLevel resolvedLevel() {
        return level != null ? level : SupportLogLevel.INFO;
    }

    /**
     * Resolves the effective masking configuration.
     *
     * @return the configured masking settings, or masking enabled with no overrides
     */
    public MaskingProperties resolvedMasking() {
        return masking != null
                ? masking
                : new MaskingProperties(true, null);
    }
}
