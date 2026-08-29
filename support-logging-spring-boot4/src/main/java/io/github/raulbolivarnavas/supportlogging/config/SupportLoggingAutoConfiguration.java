package io.github.raulbolivarnavas.supportlogging.config;

import io.github.raulbolivarnavas.supportlogging.adapter.SupportLogger;
import io.github.raulbolivarnavas.supportlogging.helper.BuildCurl;
import io.github.raulbolivarnavas.supportlogging.json.Jackson3JsonFormatter;
import io.github.raulbolivarnavas.supportlogging.json.JsonFormatter;
import io.github.raulbolivarnavas.supportlogging.masking.DataMasker;
import io.github.raulbolivarnavas.supportlogging.masking.Jackson3DataMasker;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogCapture;
import io.github.raulbolivarnavas.supportlogging.model.SupportLoggingAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

/**
 * Auto-configures the core support-logging beans for Spring Boot 4 applications.
 */
@AutoConfiguration
@EnableConfigurationProperties(SupportLoggingProperties.class)
public class SupportLoggingAutoConfiguration {

    /**
     * Creates the JSON formatter used by the support logger.
     *
     * @param jsonMapper the application JSON mapper
     * @return a Jackson 3-based JSON formatter
     */
    @Bean
    @ConditionalOnMissingBean
    public JsonFormatter jsonFormatter(JsonMapper jsonMapper) {
        return new Jackson3JsonFormatter(jsonMapper);
    }

    /**
     * Creates the data masker used to sanitize sensitive payload fields.
     *
     * @param jsonMapper the application JSON mapper
     * @param properties the support logging properties
     * @return a Jackson 3-based data masker
     */
    @Bean
    @ConditionalOnMissingBean
    public DataMasker dataMasker(
            JsonMapper jsonMapper,
            SupportLoggingProperties properties
    ) {
        return new Jackson3DataMasker(
                jsonMapper,
                properties.resolvedMasking()
        );
    }

    /**
     * Creates the cURL builder used in debug logs.
     *
     * @param jsonFormatter the JSON formatter dependency
     * @return a cURL builder
     */
    @Bean
    @ConditionalOnMissingBean
    public BuildCurl buildCurl(JsonFormatter jsonFormatter) {
        return new BuildCurl(jsonFormatter);
    }

    /**
     * Creates the main logger that renders support log messages.
     *
     * @param properties the support logging properties
     * @param jsonFormatter the JSON formatter dependency
     * @param buildCurl the cURL builder dependency
     * @param dataMasker the masking dependency
     * @return the support logger bean
     */
    @Bean
    @ConditionalOnMissingBean
    public SupportLogger supportLogger(
            SupportLoggingProperties properties,
            JsonFormatter jsonFormatter,
            BuildCurl buildCurl,
            DataMasker dataMasker
    ) {
        return new SupportLogger(
                properties,
                jsonFormatter,
                buildCurl,
                dataMasker
        );
    }

    /**
     * Creates the capture helper used inside reactive pipelines.
     *
     * @return the support log capture bean
     */
    @Bean
    @ConditionalOnMissingBean
    public SupportLogCapture supportLogCapture() {
        return new SupportLogCapture();
    }

    /**
     * Creates the aspect that intercepts {@link io.github.raulbolivarnavas.supportlogging.SupportLogging}
     * methods.
     *
     * @param supportLogger the logger used to render the final message
     * @return the support logging aspect
     */
    @Bean
    @ConditionalOnMissingBean
    public SupportLoggingAspect supportLoggingAspect(
            SupportLogger supportLogger
    ) {
        return new SupportLoggingAspect(supportLogger);
    }
}
