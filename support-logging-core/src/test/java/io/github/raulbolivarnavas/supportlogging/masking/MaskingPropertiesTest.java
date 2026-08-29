package io.github.raulbolivarnavas.supportlogging.masking;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskingPropertiesTest {

    @Test
    void resolvedValuesShouldUseDefaultsWhenMissing() {
        MaskingProperties properties = new MaskingProperties(null, null);

        assertTrue(properties.resolvedEnabled());
        assertTrue(properties.resolvedFields().isEmpty());
    }

    @Test
    void resolvedValuesShouldKeepConfiguredValues() {
        Map<String, MaskFieldProperties> fields = new LinkedHashMap<>();
        fields.put("secret", new MaskFieldProperties(MaskType.FULL, null));

        MaskingProperties properties = new MaskingProperties(false, fields);

        assertFalse(properties.resolvedEnabled());
        assertSame(fields, properties.resolvedFields());
        assertEquals(fields, properties.resolvedFields());
    }
}
