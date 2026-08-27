package io.github.raulbolivarnavas.supportlogging.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupportLogLevelTest {

    @Test
    @DisplayName("Debe tener exactamente dos valores")
    void shouldHaveExactlyTwoValues() {
        assertThat(SupportLogLevel.values()).hasSize(2);
    }

    @Test
    @DisplayName("Debe contener INFO y DEBUG")
    void shouldContainInfoAndDebug() {
        assertThat(SupportLogLevel.values())
                .containsExactlyInAnyOrder(SupportLogLevel.INFO, SupportLogLevel.DEBUG);
    }

    @Test
    @DisplayName("valueOf INFO debe retornar la constante INFO")
    void valueOfInfoShouldReturnInfoConstant() {
        assertThat(SupportLogLevel.valueOf("INFO")).isEqualTo(SupportLogLevel.INFO);
    }

    @Test
    @DisplayName("valueOf DEBUG debe retornar la constante DEBUG")
    void valueOfDebugShouldReturnDebugConstant() {
        assertThat(SupportLogLevel.valueOf("DEBUG")).isEqualTo(SupportLogLevel.DEBUG);
    }

    @Test
    @DisplayName("valueOf con valor desconocido debe lanzar IllegalArgumentException")
    void valueOfUnknownShouldThrow() {
        assertThatThrownBy(() -> SupportLogLevel.valueOf("TRACE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("name() debe retornar el nombre textual de la constante")
    void nameShouldReturnTextualName() {
        assertThat(SupportLogLevel.INFO.name()).isEqualTo("INFO");
        assertThat(SupportLogLevel.DEBUG.name()).isEqualTo("DEBUG");
    }

    @Test
    @DisplayName("ordinal debe preservar el orden de declaración")
    void ordinalShouldPreserveDeclarationOrder() {
        assertThat(SupportLogLevel.INFO.ordinal()).isLessThan(SupportLogLevel.DEBUG.ordinal());
    }
}
