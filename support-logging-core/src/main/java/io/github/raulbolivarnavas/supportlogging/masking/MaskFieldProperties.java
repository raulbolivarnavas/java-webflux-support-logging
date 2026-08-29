package io.github.raulbolivarnavas.supportlogging.masking;

/**
 * Masking rules for a single field.
 *
 * @param type the masking strategy to apply
 * @param visible the number of visible characters when partial masking is used
 */
public record MaskFieldProperties(
        MaskType type,
        Integer visible
) {
    /**
     * Resolves the effective masking strategy.
     *
     * @return the configured type, or {@link MaskType#FULL} when unspecified
     */
    public MaskType resolvedType() {
        return type != null ? type : MaskType.FULL;
    }

    /**
     * Resolves the number of visible characters for partial masking.
     *
     * @return a non-negative number of visible characters
     */
    public int resolvedVisible() {
        return visible != null && visible >= 0 ? visible : 0;
    }
}
