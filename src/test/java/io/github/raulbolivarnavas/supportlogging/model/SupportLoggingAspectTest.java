package io.github.raulbolivarnavas.supportlogging.model;

import io.github.raulbolivarnavas.supportlogging.SupportLogging;
import io.github.raulbolivarnavas.supportlogging.adapter.SupportLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportLoggingAspectTest {

    @Mock
    private SupportLogger supportLogger;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private SupportLogging supportLogging;

    @Mock
    private Signature signature;

    private SupportLoggingAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new SupportLoggingAspect(supportLogger);
    }

    @Nested
    @DisplayName("Resultados no reactivos")
    class NonReactiveResults {

        @Test
        @DisplayName("Debe retornar directamente el resultado cuando no es Mono")
        void shouldReturnResultDirectlyWhenResultIsNotMono() throws Throwable {
            String expected = "result";

            when(joinPoint.proceed()).thenReturn(expected);

            Object result = aspect.around(joinPoint, supportLogging);

            assertThat(result).isSameAs(expected);

            verify(joinPoint).proceed();
            verifyNoInteractions(supportLogger);
            verifyNoInteractions(supportLogging);
        }

        @Test
        @DisplayName("Debe retornar null cuando el método interceptado retorna null")
        void shouldReturnNullWhenInterceptedMethodReturnsNull() throws Throwable {
            when(joinPoint.proceed()).thenReturn(null);

            Object result = aspect.around(joinPoint, supportLogging);

            assertThat(result).isNull();

            verify(joinPoint).proceed();
            verifyNoInteractions(supportLogger);
            verifyNoInteractions(supportLogging);
        }
    }

    @Nested
    @DisplayName("Resolución de operación")
    class OperationResolution {

        @Test
        @DisplayName("Debe utilizar operation de la anotación cuando está informado")
        void shouldUseAnnotationOperationWhenProvided() throws Throwable {
            Mono<String> originalMono = Mono.just("response");

            when(joinPoint.proceed()).thenReturn(originalMono);
            when(supportLogging.operation()).thenReturn("retrieve-card");

            Object result = aspect.around(joinPoint, supportLogging);

            StepVerifier.create(cast(result))
                    .expectNext("response")
                    .verifyComplete();

            ArgumentCaptor<SupportLogState> captor =
                    ArgumentCaptor.forClass(SupportLogState.class);

            verify(supportLogger).log(captor.capture());

            SupportLogState state = captor.getValue();

            assertThat(state.getOperation()).isEqualTo("retrieve-card");
            assertThat(state.getResponse()).isEqualTo("response");

            verify(joinPoint, never()).getSignature();
        }

        @Test
        @DisplayName("Debe utilizar nombre del método cuando operation está vacío")
        void shouldUseMethodNameWhenOperationIsEmpty() throws Throwable {
            when(joinPoint.proceed()).thenReturn(Mono.just("response"));
            when(supportLogging.operation()).thenReturn("");
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getName()).thenReturn("retrieveCard");

            Object result = aspect.around(joinPoint, supportLogging);

            StepVerifier.create(cast(result))
                    .expectNext("response")
                    .verifyComplete();

            ArgumentCaptor<SupportLogState> captor =
                    ArgumentCaptor.forClass(SupportLogState.class);

            verify(supportLogger).log(captor.capture());

            assertThat(captor.getValue().getOperation())
                    .isEqualTo("retrieveCard");
        }

        @Test
        @DisplayName("Debe utilizar nombre del método cuando operation contiene solo espacios")
        void shouldUseMethodNameWhenOperationIsBlank() throws Throwable {
            when(joinPoint.proceed()).thenReturn(Mono.just("response"));
            when(supportLogging.operation()).thenReturn("   ");
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getName()).thenReturn("createCustomer");

            Object result = aspect.around(joinPoint, supportLogging);

            StepVerifier.create(cast(result))
                    .expectNext("response")
                    .verifyComplete();

            ArgumentCaptor<SupportLogState> captor =
                    ArgumentCaptor.forClass(SupportLogState.class);

            verify(supportLogger).log(captor.capture());

            assertThat(captor.getValue().getOperation())
                    .isEqualTo("createCustomer");
        }
    }

    @Nested
    @DisplayName("Mono exitoso")
    class SuccessfulMono {

        @Test
        @DisplayName("Debe almacenar la respuesta emitida por el Mono")
        void shouldStoreMonoResponse() throws Throwable {
            TestResponse response = new TestResponse("001", "APPROVED");

            when(joinPoint.proceed()).thenReturn(Mono.just(response));
            when(supportLogging.operation()).thenReturn("payment");

            Object result = aspect.around(joinPoint, supportLogging);

            StepVerifier.create(cast(result))
                    .expectNext(response)
                    .verifyComplete();

            ArgumentCaptor<SupportLogState> captor =
                    ArgumentCaptor.forClass(SupportLogState.class);

            verify(supportLogger).log(captor.capture());

            SupportLogState state = captor.getValue();

            assertThat(state.getOperation()).isEqualTo("payment");
            assertThat(state.getResponse()).isSameAs(response);
            assertThat(state.getError()).isNull();
        }

        @Test
        @DisplayName("Debe conservar exactamente la misma respuesta del flujo original")
        void shouldPreserveOriginalResponse() throws Throwable {
            Object response = new Object();

            when(joinPoint.proceed()).thenReturn(Mono.just(response));
            when(supportLogging.operation()).thenReturn("test");

            Mono<Object> intercepted = cast(
                    aspect.around(joinPoint, supportLogging));

            StepVerifier.create(intercepted)
                    .expectNextMatches(value -> value == response)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Debe ejecutar el logger una sola vez al completar")
        void shouldLogExactlyOnceOnCompletion() throws Throwable {
            when(joinPoint.proceed()).thenReturn(Mono.just("OK"));
            when(supportLogging.operation()).thenReturn("operation");

            Object result = aspect.around(joinPoint, supportLogging);

            StepVerifier.create(cast(result))
                    .expectNext("OK")
                    .verifyComplete();

            verify(supportLogger, times(1))
                    .log(any(SupportLogState.class));
        }
    }

    @Nested
    @DisplayName("Mono con error")
    class ErrorMono {

        @Test
        @DisplayName("Debe almacenar el error emitido por el Mono")
        void shouldStoreMonoError() throws Throwable {
            RuntimeException expectedError =
                    new RuntimeException("Service unavailable");

            when(joinPoint.proceed())
                    .thenReturn(Mono.error(expectedError));

            when(supportLogging.operation())
                    .thenReturn("retrieve-account");

            Object result = aspect.around(joinPoint, supportLogging);

            StepVerifier.create(cast(result))
                    .expectErrorMatches(error -> error == expectedError)
                    .verify();

            ArgumentCaptor<SupportLogState> captor =
                    ArgumentCaptor.forClass(SupportLogState.class);

            verify(supportLogger).log(captor.capture());

            SupportLogState state = captor.getValue();

            assertThat(state.getOperation())
                    .isEqualTo("retrieve-account");

            assertThat(state.getError())
                    .isSameAs(expectedError);

            assertThat(state.getResponse())
                    .isNull();
        }

        @Test
        @DisplayName("No debe consumir ni transformar el error original")
        void shouldPreserveOriginalError() throws Throwable {
            IllegalStateException expected =
                    new IllegalStateException("T24 connection error");

            when(joinPoint.proceed()).thenReturn(Mono.error(expected));
            when(supportLogging.operation()).thenReturn("t24");

            Object result = aspect.around(joinPoint, supportLogging);

            StepVerifier.create(cast(result))
                    .expectErrorSatisfies(error ->
                            assertThat(error).isSameAs(expected))
                    .verify();
        }

        @Test
        @DisplayName("Debe registrar una sola vez cuando ocurre un error")
        void shouldLogExactlyOnceOnError() throws Throwable {
            when(joinPoint.proceed())
                    .thenReturn(Mono.error(new RuntimeException("error")));

            when(supportLogging.operation())
                    .thenReturn("operation");

            Object result = aspect.around(joinPoint, supportLogging);

            StepVerifier.create(cast(result))
                    .expectError()
                    .verify();

            verify(supportLogger, times(1))
                    .log(any(SupportLogState.class));
        }
    }

    @Nested
    @DisplayName("Mono vacío")
    class EmptyMono {

        @Test
        @DisplayName("Debe registrar aun cuando el Mono termina vacío")
        void shouldLogWhenMonoCompletesEmpty() throws Throwable {
            when(joinPoint.proceed()).thenReturn(Mono.empty());
            when(supportLogging.operation()).thenReturn("find-customer");

            Object result = aspect.around(joinPoint, supportLogging);

            StepVerifier.create(cast(result))
                    .verifyComplete();

            ArgumentCaptor<SupportLogState> captor =
                    ArgumentCaptor.forClass(SupportLogState.class);

            verify(supportLogger).log(captor.capture());

            SupportLogState state = captor.getValue();

            assertThat(state.getOperation())
                    .isEqualTo("find-customer");

            assertThat(state.getResponse())
                    .isNull();

            assertThat(state.getError())
                    .isNull();
        }
    }

    @Nested
    @DisplayName("Reactor Context")
    class ReactorContext {

        @Test
        @DisplayName("Debe publicar SupportLogState dentro del Reactor Context")
        void shouldPutSupportLogStateIntoReactorContext() throws Throwable {
            Mono<SupportLogState> originalMono =
                    Mono.deferContextual(contextView ->
                            Mono.just(
                                    contextView.get(
                                            SupportLogState.class)));

            when(joinPoint.proceed()).thenReturn(originalMono);
            when(supportLogging.operation())
                    .thenReturn("context-operation");

            Object result = aspect.around(joinPoint, supportLogging);

            Mono<SupportLogState> stateMono = cast(result);
            StepVerifier.create(stateMono)
                    .assertNext(state -> {
                        assertThat(state).isNotNull();
                        assertThat(state.getOperation())
                                .isEqualTo("context-operation");
                    })
                    .verifyComplete();

            verify(supportLogger)
                    .log(any(SupportLogState.class));
        }

        @Test
        @DisplayName("El SupportLogState del Context debe ser el mismo enviado al logger")
        void shouldUseSameStateFromContextAndLogger() throws Throwable {
            Mono<SupportLogState> originalMono =
                    Mono.deferContextual(context ->
                            Mono.just(
                                    context.get(SupportLogState.class)));

            when(joinPoint.proceed()).thenReturn(originalMono);
            when(supportLogging.operation()).thenReturn("operation");

            Object result = aspect.around(joinPoint, supportLogging);

            final SupportLogState[] contextState =
                    new SupportLogState[1];

            StepVerifier.create(cast(result))
                    .assertNext(state -> contextState[0] = (SupportLogState) state)
                    .verifyComplete();

            ArgumentCaptor<SupportLogState> captor =
                    ArgumentCaptor.forClass(SupportLogState.class);

            verify(supportLogger)
                    .log(captor.capture());

            assertThat(captor.getValue())
                    .isSameAs(contextState[0]);
        }
    }

    @Nested
    @DisplayName("JoinPoint")
    class JoinPointExecution {

        @Test
        @DisplayName("Debe ejecutar proceed exactamente una vez")
        void shouldProceedExactlyOnce() throws Throwable {
            when(joinPoint.proceed()).thenReturn(Mono.just("OK"));
            when(supportLogging.operation()).thenReturn("operation");

            Object result = aspect.around(joinPoint, supportLogging);

            StepVerifier.create(cast(result))
                    .expectNext("OK")
                    .verifyComplete();

            verify(joinPoint, times(1)).proceed();
        }

        @Test
        @DisplayName("Debe propagar excepción si proceed falla antes de retornar Mono")
        void shouldPropagateExceptionThrownByProceed() throws Throwable {
            RuntimeException expected =
                    new RuntimeException("Invocation failed");

            when(joinPoint.proceed()).thenThrow(expected);

            assertThatThrownBy(
                    () -> aspect.around(joinPoint, supportLogging))
                    .isSameAs(expected);

            verifyNoInteractions(supportLogger);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<T> cast(Object value) {
        return (Mono<T>) value;
    }

    private record TestResponse(
            String code,
            String status) {
    }
}