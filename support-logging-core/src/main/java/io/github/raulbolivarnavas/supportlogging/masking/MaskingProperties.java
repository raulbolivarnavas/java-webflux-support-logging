package io.github.raulbolivarnavas.supportlogging.masking;

import java.util.Map;

/**
 * Global masking configuration and per-field overrides.
 *
 * @param enabled whether masking is active
 * @param fields the configured field-level masking rules
 */
public record MaskingProperties(
        Boolean enabled,
        Map<String, MaskFieldProperties> fields
) {
    /**
     * Resolves whether masking should run.
     *
     * @return {@code true} when masking is enabled or unspecified
     */
    public boolean resolvedEnabled() {
        return enabled == null || enabled;
    }

    /**
     * Resolves the configured field overrides.
     *
     * @return the configured fields, or an empty map when none were provided
     */
    public Map<String, MaskFieldProperties> resolvedFields() {
        return fields != null ? fields : Map.of();
    }
}
