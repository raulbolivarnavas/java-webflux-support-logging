package io.github.raulbolivarnavas.supportlogging.masking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaskFieldPropertiesTest {

    @Test
    void resolvedValuesShouldUseDefaultsWhenMissing() {
        MaskFieldProperties properties = new MaskFieldProperties(null, null);

        assertEquals(MaskType.FULL, properties.resolvedType());
        assertEquals(0, properties.resolvedVisible());
    }

    @Test
    void resolvedValuesShouldKeepConfiguredValues() {
        MaskFieldProperties properties = new MaskFieldProperties(MaskType.RIGHT, 3);

        assertEquals(MaskType.RIGHT, properties.resolvedType());
        assertEquals(3, properties.resolvedVisible());
    }

    @Test
    void resolvedVisibleShouldClampNegativeValuesToZero() {
        MaskFieldProperties properties = new MaskFieldProperties(MaskType.LEFT, -1);

        assertEquals(0, properties.resolvedVisible());
    }
}
