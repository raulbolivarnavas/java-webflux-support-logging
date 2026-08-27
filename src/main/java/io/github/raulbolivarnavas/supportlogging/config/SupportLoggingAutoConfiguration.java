package io.github.raulbolivarnavas.supportlogging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.raulbolivarnavas.supportlogging.adapter.SupportLogger;
import io.github.raulbolivarnavas.supportlogging.helper.BuildCurl;
import io.github.raulbolivarnavas.supportlogging.masking.DataMasker;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogCapture;
import io.github.raulbolivarnavas.supportlogging.model.SupportLoggingAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring autoconfiguration class for support logging functionality.
 * This class sets up the necessary beans for the support logging library when the application starts,
 * including the cURL builder and the support logger.
 */
@AutoConfiguration
@EnableConfigurationProperties(SupportLoggingProperties.class)
@SuppressWarnings("javadoc")
public class SupportLoggingAutoConfiguration {

    /**
     * Creates a BuildCurl bean for constructing cURL command representations of HTTP requests.
     *
     * @param objectMapper the Jackson ObjectMapper for JSON serialization
     * @return a new BuildCurl instance
     */
    @Bean
    @ConditionalOnMissingBean
    public BuildCurl buildCurl(ObjectMapper objectMapper) {
        return new BuildCurl(objectMapper);
    }

    /**
     * Creates a SupportLogger bean for logging support operations in a structured format.
     *
     * @param properties the support logging configuration properties
     * @param objectMapper the Jackson ObjectMapper for JSON serialization
     * @param buildCurl the BuildCurl helper for cURL command generation
     * @return a new SupportLogger instance
     */
    @Bean
    @ConditionalOnMissingBean
    public SupportLogger supportLogger(
            SupportLoggingProperties properties,
            ObjectMapper objectMapper,
            BuildCurl buildCurl,
            DataMasker dataMasker
    ) {
        return new SupportLogger(
                properties,
                objectMapper,
                buildCurl,
                dataMasker
        );
    }

    /**
     * Create a SupportLogCapture bean for logging support capture data.
     *
     * @return a new SupportLogCapture instance
     */
    @Bean
    @ConditionalOnMissingBean
    public SupportLogCapture supportLogCapture() {
        return new SupportLogCapture();
    }

    /**
     * Create a SupportLoggingAspect bean for logging support
     *
     * @param supportLogger a SupportLogger instance
     * @return a new SupportLoggingAspect instance
     */
    @Bean
    @ConditionalOnMissingBean
    public SupportLoggingAspect supportLoggingAspect(SupportLogger supportLogger) {
        return new SupportLoggingAspect(
                supportLogger
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public DataMasker dataMasker(
            ObjectMapper objectMapper,
            SupportLoggingProperties properties
    ) {
        return new DataMasker(
                objectMapper,
                properties
        );
    }
}
