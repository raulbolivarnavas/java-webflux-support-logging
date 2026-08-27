package io.github.raulbolivarnavas.supportlogging.model;

import io.github.raulbolivarnavas.supportlogging.dto.SupportLogData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import static org.assertj.core.api.Assertions.assertThat;

class SupportLogContextTest {

    private final SupportLogContext supportLogContext = new SupportLogContext();

    @Test
    @DisplayName("KEY debe ser el nombre completo de la clase SupportLogData")
    void keyShouldBeFullyQualifiedClassNameOfSupportLogData() {
        assertThat(SupportLogContext.KEY).isEqualTo(SupportLogData.class.getName());
    }

    @Nested
    @DisplayName("get — clave presente en el contexto")
    class WhenKeyIsPresent {

        @Test
        @DisplayName("Debe retornar el mismo objeto almacenado en el contexto")
        void shouldReturnStoredObject() {
            SupportLogData stored = SupportLogData.builder()
                    .operation("retrieve-account")
                    .method("POST")
                    .url("https://api.test")
                    .build();

            ContextView ctx = Context.of(SupportLogContext.KEY, stored);

            SupportLogData result = supportLogContext.get(ctx);

            assertThat(result).isSameAs(stored);
        }

        @Test
        @DisplayName("Debe retornar exactamente los datos guardados, sin transformación")
        void shouldReturnDataWithoutModification() {
            SupportLogData stored = SupportLogData.builder()
                    .operation("transfer")
                    .method("PUT")
                    .url("https://bank.api/transfer")
                    .build();

            ContextView ctx = Context.of(SupportLogContext.KEY, stored);

            SupportLogData result = supportLogContext.get(ctx);

            assertThat(result.operation()).isEqualTo("transfer");
            assertThat(result.method()).isEqualTo("PUT");
            assertThat(result.url()).isEqualTo("https://bank.api/transfer");
        }
    }

    @Nested
    @DisplayName("get — clave ausente en el contexto")
    class WhenKeyIsAbsent {

        @Test
        @DisplayName("Debe retornar un SupportLogData vacío como valor por defecto")
        void shouldReturnEmptyDefaultWhenKeyNotPresent() {
            ContextView emptyCtx = Context.empty();

            SupportLogData result = supportLogContext.get(emptyCtx);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("El valor por defecto debe tener todos sus campos en null")
        void defaultValueShouldHaveNullFields() {
            ContextView emptyCtx = Context.empty();

            SupportLogData result = supportLogContext.get(emptyCtx);

            assertThat(result.operation()).isNull();
            assertThat(result.method()).isNull();
            assertThat(result.url()).isNull();
            assertThat(result.headers()).isNull();
            assertThat(result.request()).isNull();
            assertThat(result.response()).isNull();
            assertThat(result.error()).isNull();
        }

        @Test
        @DisplayName("Contexto con otras claves no debe afectar el valor por defecto")
        void contextWithOtherKeysShouldStillReturnDefault() {
            ContextView ctx = Context.of("other.key", "some-value");

            SupportLogData result = supportLogContext.get(ctx);

            assertThat(result).isNotNull();
            assertThat(result.operation()).isNull();
        }
    }
}
