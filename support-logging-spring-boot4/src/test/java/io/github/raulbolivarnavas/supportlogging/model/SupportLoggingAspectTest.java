package io.github.raulbolivarnavas.supportlogging.model;

import io.github.raulbolivarnavas.supportlogging.SupportLogging;
import io.github.raulbolivarnavas.supportlogging.adapter.SupportLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupportLoggingAspectTest {

    private final SupportLogger supportLogger = mock(SupportLogger.class);
    private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    private final Signature signature = mock(Signature.class);
    private final SupportLogging supportLogging = mock(SupportLogging.class);
    private final SupportLoggingAspect aspect = new SupportLoggingAspect(supportLogger);

    @Test
    void shouldReturnOriginalResultWhenInvocationIsNotReactive() throws Throwable {
        when(joinPoint.proceed()).thenReturn("value");

        Object result = aspect.around(joinPoint, supportLogging);

        assertEquals("value", result);
        verify(joinPoint).proceed();
    }

    @Test
    void shouldCaptureMonoResultUsingMethodNameWhenOperationIsBlank() throws Throwable {
        when(joinPoint.proceed()).thenReturn(Mono.just("value"));
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("methodName");
        when(supportLogging.operation()).thenReturn("");

        Object result = aspect.around(joinPoint, supportLogging);

        @SuppressWarnings("unchecked")
        Mono<String> mono = (Mono<String>) result;
        StepVerifier.create(mono).expectNext("value").verifyComplete();

        ArgumentCaptor<SupportLogState> captor = ArgumentCaptor.forClass(SupportLogState.class);
        verify(supportLogger).log(captor.capture());
        assertEquals("methodName", captor.getValue().getOperation());
        assertEquals("value", captor.getValue().getResponse());
        assertNull(captor.getValue().getError());
    }

    @Test
    void shouldCaptureMonoErrorUsingCustomOperation() throws Throwable {
        when(joinPoint.proceed()).thenReturn(Mono.error(new IllegalStateException("boom")));
        when(supportLogging.operation()).thenReturn("custom-operation");

        Object result = aspect.around(joinPoint, supportLogging);

        StepVerifier.create((Mono<?>) result).expectErrorMatches(
                error -> error instanceof IllegalStateException
                        && "boom".equals(error.getMessage())
        ).verify();

        ArgumentCaptor<SupportLogState> captor = ArgumentCaptor.forClass(SupportLogState.class);
        verify(supportLogger).log(captor.capture());
        assertEquals("custom-operation", captor.getValue().getOperation());
        assertEquals("boom", captor.getValue().getError().getMessage());
    }
}
