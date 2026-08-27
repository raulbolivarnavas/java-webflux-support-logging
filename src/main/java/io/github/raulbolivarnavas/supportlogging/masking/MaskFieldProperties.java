package io.github.raulbolivarnavas.supportlogging.masking;

public record MaskFieldProperties(
        MaskType type,
        Integer visible
) {

    public MaskType resolvedType() {
        return type != null
                ? type
                : MaskType.FULL;
    }

    public int resolvedVisible() {
        return visible != null && visible >= 0
                ? visible
                : 0;
    }
}
