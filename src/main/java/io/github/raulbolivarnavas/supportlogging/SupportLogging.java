package io.github.raulbolivarnavas.supportlogging;

import java.lang.annotation.*;

/**
 * Annotation to indicate that a method supports logging.
 * This annotation can be used to mark methods that require logging of their execution details,
 * such as input parameters, output results, and any exceptions thrown during execution.
 * The operation attribute can be used to specify a custom operation name for logging purposes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SupportLogging {

    /**
     * Specifies the operation name for logging purposes.
     * @return the operation name as a String
     */
    String operation() default "";
}
