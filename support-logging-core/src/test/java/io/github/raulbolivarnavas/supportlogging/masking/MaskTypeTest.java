package io.github.raulbolivarnavas.supportlogging.masking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MaskTypeTest {

    @Test
    void shouldExposeAllMaskTypes() {
        assertArrayEquals(
                new MaskType[]{MaskType.FULL, MaskType.LEFT, MaskType.RIGHT, MaskType.NONE},
                MaskType.values()
        );
    }
}
