package io.github.raulbolivarnavas.supportlogging.config;

import io.github.raulbolivarnavas.supportlogging.masking.MaskingProperties;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportLoggingPropertiesTest {

    @Test
    void shouldResolveDefaultValues() {
        SupportLoggingProperties properties = new SupportLoggingProperties(null, null);

        assertEquals(SupportLogLevel.INFO, properties.resolvedLevel());
        assertTrue(properties.resolvedMasking().resolvedEnabled());
        assertTrue(properties.resolvedMasking().resolvedFields().isEmpty());
    }

    @Test
    void shouldKeepConfiguredValues() {
        MaskingProperties maskingProperties = new MaskingProperties(false, null);
        SupportLoggingProperties properties =
                new SupportLoggingProperties(SupportLogLevel.DEBUG, maskingProperties);

        assertEquals(SupportLogLevel.DEBUG, properties.resolvedLevel());
        assertEquals(maskingProperties, properties.resolvedMasking());
    }
}
