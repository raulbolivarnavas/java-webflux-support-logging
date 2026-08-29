package io.github.raulbolivarnavas.supportlogging;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportLoggingAnnotationTest {

    @Test
    void shouldTargetMethodsAndBeRetainedAtRuntime() {
        Target target = SupportLogging.class.getAnnotation(Target.class);
        Retention retention = SupportLogging.class.getAnnotation(Retention.class);
        Documented documented = SupportLogging.class.getAnnotation(Documented.class);

        assertArrayEquals(new ElementType[]{ElementType.METHOD}, target.value());
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
        assertTrue(documented != null);
        assertEquals("", SupportLogging.class.getDeclaredMethods()[0].getDefaultValue());
    }
}
