package io.github.raulbolivarnavas.supportlogging.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SupportLogStateTest {

    @Nested
    @DisplayName("Constructor sin argumentos")
    class NoArgsConstructor {

        @Test
        @DisplayName("Debe crear un estado con todos los campos en null")
        void shouldCreateStateWithAllFieldsNull() {
            SupportLogState state = new SupportLogState();

            assertThat(state.getOperation()).isNull();
            assertThat(state.getMethod()).isNull();
            assertThat(state.getUrl()).isNull();
            assertThat(state.getQueryParams()).isNull();
            assertThat(state.getHeaders()).isNull();
            assertThat(state.getRequest()).isNull();
            assertThat(state.getResponse()).isNull();
            assertThat(state.getError()).isNull();
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("Debe asignar todos los campos correctamente")
        void shouldSetAllFieldsViaBuilder() {
            RuntimeException error = new RuntimeException("fail");
            Map<String, String> queryParams = Map.of("page", "1");
            Map<String, String> headers = Map.of("Authorization", "Bearer token");
            Object request = Map.of("id", "100");
            Object response = Map.of("result", "OK");

            SupportLogState state = SupportLogState.builder()
                    .operation("retrieve-card")
                    .method("POST")
                    .url("https://api.test/cards")
                    .queryParams(queryParams)
                    .headers(headers)
                    .request(request)
                    .response(response)
                    .error(error)
                    .build();

            assertThat(state.getOperation()).isEqualTo("retrieve-card");
            assertThat(state.getMethod()).isEqualTo("POST");
            assertThat(state.getUrl()).isEqualTo("https://api.test/cards");
            assertThat(state.getQueryParams()).isEqualTo(queryParams);
            assertThat(state.getHeaders()).isEqualTo(headers);
            assertThat(state.getRequest()).isEqualTo(request);
            assertThat(state.getResponse()).isEqualTo(response);
            assertThat(state.getError()).isSameAs(error);
        }

        @Test
        @DisplayName("Debe permitir construir con campos parciales")
        void shouldAllowPartialBuild() {
            SupportLogState state = SupportLogState.builder()
                    .operation("op")
                    .method("GET")
                    .build();

            assertThat(state.getOperation()).isEqualTo("op");
            assertThat(state.getMethod()).isEqualTo("GET");
            assertThat(state.getUrl()).isNull();
            assertThat(state.getRequest()).isNull();
            assertThat(state.getResponse()).isNull();
            assertThat(state.getError()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor con todos los argumentos")
    class AllArgsConstructor {

        @Test
        @DisplayName("Debe asignar todos los campos en el orden correcto")
        void shouldAssignAllFieldsInOrder() {
            RuntimeException error = new RuntimeException("err");
            Map<String, String> qp = Map.of("q", "search");
            Map<String, String> h = Map.of("X-ID", "abc");

            SupportLogState state = new SupportLogState(
                    "operation", "DELETE", "https://test", qp, h, "req", "resp", error
            );

            assertThat(state.getOperation()).isEqualTo("operation");
            assertThat(state.getMethod()).isEqualTo("DELETE");
            assertThat(state.getUrl()).isEqualTo("https://test");
            assertThat(state.getQueryParams()).isEqualTo(qp);
            assertThat(state.getHeaders()).isEqualTo(h);
            assertThat(state.getRequest()).isEqualTo("req");
            assertThat(state.getResponse()).isEqualTo("resp");
            assertThat(state.getError()).isSameAs(error);
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("Debe actualizar cada campo individualmente")
        void shouldUpdateEachFieldIndependently() {
            SupportLogState state = new SupportLogState();
            RuntimeException error = new RuntimeException("fail");
            Map<String, String> headers = Map.of("X-Trace", "123");

            state.setOperation("op");
            state.setMethod("PUT");
            state.setUrl("https://service/resource");
            state.setHeaders(headers);
            state.setRequest("requestBody");
            state.setResponse("responseBody");
            state.setError(error);

            assertThat(state.getOperation()).isEqualTo("op");
            assertThat(state.getMethod()).isEqualTo("PUT");
            assertThat(state.getUrl()).isEqualTo("https://service/resource");
            assertThat(state.getHeaders()).isEqualTo(headers);
            assertThat(state.getRequest()).isEqualTo("requestBody");
            assertThat(state.getResponse()).isEqualTo("responseBody");
            assertThat(state.getError()).isSameAs(error);
        }

        @Test
        @DisplayName("Debe permitir sobreescribir un campo ya asignado")
        void shouldAllowOverwritingAnAlreadySetField() {
            SupportLogState state = SupportLogState.builder()
                    .operation("original")
                    .build();

            state.setOperation("updated");

            assertThat(state.getOperation()).isEqualTo("updated");
        }

        @Test
        @DisplayName("Debe permitir asignar null a cualquier campo")
        void shouldAllowSettingNull() {
            SupportLogState state = SupportLogState.builder()
                    .operation("op")
                    .method("GET")
                    .response("resp")
                    .build();

            state.setOperation(null);
            state.setMethod(null);
            state.setResponse(null);

            assertThat(state.getOperation()).isNull();
            assertThat(state.getMethod()).isNull();
            assertThat(state.getResponse()).isNull();
        }
    }
}
