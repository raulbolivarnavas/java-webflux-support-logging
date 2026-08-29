package io.github.raulbolivarnavas.supportlogging.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SupportLogLevelTest {

    @Test
    void shouldExposeAllSupportLogLevels() {
        assertArrayEquals(
                new SupportLogLevel[]{SupportLogLevel.INFO, SupportLogLevel.DEBUG},
                SupportLogLevel.values()
        );
    }
}
