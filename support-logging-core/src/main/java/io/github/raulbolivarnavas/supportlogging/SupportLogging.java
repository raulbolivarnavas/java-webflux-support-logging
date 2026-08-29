package io.github.raulbolivarnavas.supportlogging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a reactive service method so its request and response can be captured
 * and logged by the support-logging aspect.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SupportLogging {
    /**
     * Overrides the operation name used in the log output.
     *
     * @return the custom operation name, or an empty string to use the method name
     */
    String operation() default "";
}
