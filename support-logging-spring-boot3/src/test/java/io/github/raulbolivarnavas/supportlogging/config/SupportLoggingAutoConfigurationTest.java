package io.github.raulbolivarnavas.supportlogging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.raulbolivarnavas.supportlogging.adapter.SupportLogger;
import io.github.raulbolivarnavas.supportlogging.helper.BuildCurl;
import io.github.raulbolivarnavas.supportlogging.json.Jackson2JsonFormatter;
import io.github.raulbolivarnavas.supportlogging.json.JsonFormatter;
import io.github.raulbolivarnavas.supportlogging.masking.DataMasker;
import io.github.raulbolivarnavas.supportlogging.masking.Jackson2DataMasker;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogCapture;
import io.github.raulbolivarnavas.supportlogging.model.SupportLoggingAspect;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportLoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SupportLoggingAutoConfiguration.class))
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void shouldCreateAllBeansAndBindProperties() {
        contextRunner
                .withPropertyValues(
                        "support.logging.level=DEBUG",
                        "support.logging.masking.enabled=false"
                )
                .run(context -> {
                    assertTrue(context.getBean(JsonFormatter.class) instanceof Jackson2JsonFormatter);
                    assertTrue(context.getBean(DataMasker.class) instanceof Jackson2DataMasker);
                    assertTrue(context.getBean(BuildCurl.class) != null);
                    assertTrue(context.getBean(SupportLogger.class) != null);
                    assertTrue(context.getBean(SupportLogCapture.class) != null);
                    assertTrue(context.getBean(SupportLoggingAspect.class) != null);

                    SupportLoggingProperties properties = context.getBean(SupportLoggingProperties.class);
                    assertEquals("DEBUG", properties.resolvedLevel().name());
                    assertEquals(false, properties.resolvedMasking().resolvedEnabled());
                });
    }
}
