package io.github.raulbolivarnavas.supportlogging.model;

import io.github.raulbolivarnavas.supportlogging.SupportLogging;
import io.github.raulbolivarnavas.supportlogging.adapter.SupportLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import reactor.core.publisher.Mono;

/**
 * AspectJ aspect that intercepts methods annotated with @SupportLogging.
 * This aspect logs the support log state before and after method execution,
 * handling both successful completions and error cases in reactive contexts.
 * The aspect integrates with the Mono reactive stream to capture request/response details.
 */
@Aspect
public class SupportLoggingAspect {

    private final SupportLogger supportLogger;

    /**
     * Constructs a new SupportLoggingAspect with the provided SupportLogger dependency.
     *
     * @param supportLogger the component responsible for logging support operations
     */
    public SupportLoggingAspect(SupportLogger supportLogger) {
        this.supportLogger = supportLogger;
    }

    /**
     * Around advice that intercepts methods annotated with @SupportLogging.
     * Logs the support log state before and after method execution, handling both successful and error cases.
     * If the method returns a Mono, this advice wraps it to capture context information.
     * For non-Mono returns, the method is executed without modification.
     *
     * @param joinPoint The join point representing the method execution.
     * @param supportLogging The SupportLogging annotation instance attached to the method.
     * @return The result of the method execution, potentially wrapped in a Mono with logging context.
     * @throws Throwable If the method execution throws an exception that is not handled by the reactive stream.
     */
    @Around("@annotation(supportLogging)")
    public Object around(ProceedingJoinPoint joinPoint,
                         SupportLogging supportLogging) throws Throwable {

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
                .doFinally(signal -> supportLogger.log(state))
                .contextWrite(context -> context.put(SupportLogState.class, state));
    }
}