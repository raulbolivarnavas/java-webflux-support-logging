package io.github.raulbolivarnavas.supportlogging.masking;

import java.util.Map;

public record MaskingProperties(
        Boolean enabled,
        Map<String, MaskFieldProperties> fields
) {

    public boolean resolvedEnabled() {
        return enabled == null || enabled;
    }

    public Map<String, MaskFieldProperties> resolvedFields() {
        return fields != null
                ? fields
                : Map.of();
    }
}
