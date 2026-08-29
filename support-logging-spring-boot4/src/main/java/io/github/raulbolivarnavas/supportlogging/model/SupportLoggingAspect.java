package io.github.raulbolivarnavas.supportlogging.model;

import io.github.raulbolivarnavas.supportlogging.SupportLogging;
import io.github.raulbolivarnavas.supportlogging.adapter.SupportLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import reactor.core.publisher.Mono;

/**
 * Aspect that captures the response of annotated reactive methods for logging.
 */
@Aspect
public class SupportLoggingAspect {

    private final SupportLogger supportLogger;

    /**
     * Creates a new aspect instance.
     *
     * @param supportLogger the logger used to flush the captured state
     */
    public SupportLoggingAspect(SupportLogger supportLogger) {
        this.supportLogger = supportLogger;
    }

    /**
     * Wraps annotated methods and attaches a capture state to the reactive context.
     *
     * @param joinPoint the intercepted method invocation
     * @param supportLogging the annotation that triggered the advice
     * @return the original result, or a decorated {@link Mono} when the method is reactive
     * @throws Throwable if the intercepted method fails
     */
    @Around("@annotation(supportLogging)")
    public Object around(
            ProceedingJoinPoint joinPoint,
            SupportLogging supportLogging
    ) throws Throwable {

        Object result = joinPoint.proceed();

        if (!(result instanceof Mono<?> mono)) {
            return result;
        }

        SupportLogState state = new SupportLogState();

        String operation = supportLogging.operation().isBlank()
                ? joinPoint.getSignature().getName()
                : supportLogging.operation();

        state.setOperation(operation);

        return mono
                .doOnNext(state::setResponse)
                .doOnError(state::setError)
                .doFinally(signalType -> supportLogger.log(state))
                .contextWrite(context ->
                        context.put(SupportLogState.class, state)
                );
    }
}
