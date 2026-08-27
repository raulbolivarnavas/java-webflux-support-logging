package io.github.raulbolivarnavas.supportlogging.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SupportLogCaptureTest {

    private final SupportLogCapture capture = new SupportLogCapture();

    // ─── request() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("request — con SupportLogState en el contexto")
    class RequestWithState {

        @Test
        @DisplayName("Debe completar vacío (Mono<Void>)")
        void shouldCompleteEmpty() {
            SupportLogState state = new SupportLogState();

            StepVerifier.create(
                    capture.request("POST", "https://api.test", null, null, null)
                            .contextWrite(Context.of(SupportLogState.class, state))
            ).verifyComplete();
        }

        @Test
        @DisplayName("Debe almacenar el método HTTP en el estado")
        void shouldStoreMethodInState() {
            SupportLogState state = new SupportLogState();

            StepVerifier.create(
                    capture.request("PUT", "https://api.test", null, null, null)
                            .contextWrite(Context.of(SupportLogState.class, state))
            ).verifyComplete();

            assertThat(state.getMethod()).isEqualTo("PUT");
        }

        @Test
        @DisplayName("Debe almacenar la URL en el estado")
        void shouldStoreUrlInState() {
            SupportLogState state = new SupportLogState();

            StepVerifier.create(
                    capture.request("GET", "https://bank.api/accounts", null, null, null)
                            .contextWrite(Context.of(SupportLogState.class, state))
            ).verifyComplete();

            assertThat(state.getUrl()).isEqualTo("https://bank.api/accounts");
        }

        @Test
        @DisplayName("Debe almacenar los queryParams en el estado")
        void shouldStoreQueryParamsInState() {
            SupportLogState state = new SupportLogState();
            Map<String, String> queryParams = Map.of("page", "1", "size", "20");

            StepVerifier.create(
                    capture.request("GET", "https://api.test", queryParams, null, null)
                            .contextWrite(Context.of(SupportLogState.class, state))
            ).verifyComplete();

            assertThat(state.getQueryParams()).isEqualTo(queryParams);
        }

        @Test
        @DisplayName("Debe almacenar los headers en el estado")
        void shouldStoreHeadersInState() {
            SupportLogState state = new SupportLogState();
            Map<String, String> headers = Map.of("Authorization", "Bearer token", "X-Channel", "WEB");

            StepVerifier.create(
                    capture.request("POST", "https://api.test", null, headers, null)
                            .contextWrite(Context.of(SupportLogState.class, state))
            ).verifyComplete();

            assertThat(state.getHeaders()).isEqualTo(headers);
        }

        @Test
        @DisplayName("Debe almacenar el body de la petición en el estado")
        void shouldStoreRequestBodyInState() {
            SupportLogState state = new SupportLogState();
            Object body = Map.of("cardNumber", "4111111111111111", "amount", 100.0);

            StepVerifier.create(
                    capture.request("POST", "https://api.test", null, null, body)
                            .contextWrite(Context.of(SupportLogState.class, state))
            ).verifyComplete();

            assertThat(state.getRequest()).isEqualTo(body);
        }

        @Test
        @DisplayName("Debe almacenar todos los campos simultáneamente")
        void shouldStoreAllFieldsTogether() {
            SupportLogState state = new SupportLogState();
            Map<String, String> queryParams = Map.of("q", "search");
            Map<String, String> headers = Map.of("Authorization", "Bearer token");
            Object body = Map.of("key", "value");

            StepVerifier.create(
                    capture.request("POST", "https://api.test/resource", queryParams, headers, body)
                            .contextWrite(Context.of(SupportLogState.class, state))
            ).verifyComplete();

            assertThat(state.getMethod()).isEqualTo("POST");
            assertThat(state.getUrl()).isEqualTo("https://api.test/resource");
            assertThat(state.getQueryParams()).isEqualTo(queryParams);
            assertThat(state.getHeaders()).isEqualTo(headers);
            assertThat(state.getRequest()).isEqualTo(body);
        }
    }

    @Nested
    @DisplayName("request — sin SupportLogState en el contexto")
    class RequestWithoutState {

        @Test
        @DisplayName("Debe completar sin error aunque no haya estado en el contexto")
        void shouldCompleteWithoutErrorWhenNoStateInContext() {
            StepVerifier.create(
                    capture.request("POST", "https://api.test", null, null, "body")
            ).verifyComplete();
        }

        @Test
        @DisplayName("Debe completar correctamente con contexto vacío")
        void shouldCompleteWithEmptyContext() {
            StepVerifier.create(
                    capture.request("GET", "https://api.test", Map.of(), Map.of(), null)
                            .contextWrite(Context.empty())
            ).verifyComplete();
        }
    }

    // ─── response() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("response — con SupportLogState en el contexto")
    class ResponseWithState {

        @Test
        @DisplayName("Debe retransmitir el mismo elemento que recibe")
        void shouldPassThroughTheResponseItem() {
            SupportLogState state = new SupportLogState();
            String payload = "response-payload";

            StepVerifier.create(
                    capture.<String>response(payload)
                            .contextWrite(Context.of(SupportLogState.class, state))
            )
            .assertNext(r -> assertThat(r).isSameAs(payload))
            .verifyComplete();
        }

        @Test
        @DisplayName("Debe almacenar la respuesta en el estado")
        void shouldStoreResponseInState() {
            SupportLogState state = new SupportLogState();
            Object responseBody = Map.of("status", "APPROVED", "transactionId", "TX-001");

            StepVerifier.create(
                    capture.response(responseBody)
                            .contextWrite(Context.of(SupportLogState.class, state))
            )
            .assertNext(r -> assertThat(r).isSameAs(responseBody))
            .verifyComplete();

            assertThat(state.getResponse()).isSameAs(responseBody);
        }

        @Test
        @DisplayName("Debe almacenar exactamente el mismo objeto (sin copia)")
        void shouldStoreSameReferenceNotACopy() {
            SupportLogState state = new SupportLogState();
            Object response = new Object();

            StepVerifier.create(
                    capture.response(response)
                            .contextWrite(Context.of(SupportLogState.class, state))
            ).assertNext(r -> {
            }).verifyComplete();

            assertThat(state.getResponse()).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("response — sin SupportLogState en el contexto")
    class ResponseWithoutState {

        @Test
        @DisplayName("Debe retransmitir el elemento aunque no haya estado en el contexto")
        void shouldPassThroughItemWithoutState() {
            String payload = "payload";

            StepVerifier.create(capture.<String>response(payload))
                    .assertNext(r -> assertThat(r).isSameAs(payload))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Debe funcionar con cualquier tipo de objeto")
        void shouldWorkWithAnyType() {
            Integer value = 42;

            StepVerifier.create(capture.<Integer>response(value))
                    .assertNext(r -> assertThat(r).isEqualTo(42))
                    .verifyComplete();
        }
    }

    // ─── Integración request + response ───────────────────────────────────────

    @Nested
    @DisplayName("Integración: request y response sobre el mismo estado")
    class IntegrationRequestAndResponse {

        @Test
        @DisplayName("Ambas operaciones deben escribir en el mismo SupportLogState del contexto")
        void bothOperationsShouldWriteToSameState() {
            SupportLogState state = new SupportLogState();
            Object requestBody = Map.of("card", "4111111111111111");
            Object responseBody = Map.of("approved", true);

            Mono<Object> pipeline = capture
                    .request("POST", "https://api.test/charge", null, null, requestBody)
                    .then(capture.response(responseBody))
                    .contextWrite(Context.of(SupportLogState.class, state));

            StepVerifier.create(pipeline)
                    .assertNext(r -> assertThat(r).isSameAs(responseBody))
                    .verifyComplete();

            assertThat(state.getMethod()).isEqualTo("POST");
            assertThat(state.getUrl()).isEqualTo("https://api.test/charge");
            assertThat(state.getRequest()).isEqualTo(requestBody);
            assertThat(state.getResponse()).isSameAs(responseBody);
        }
    }
}
