package io.github.raulbolivarnavas.supportlogging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.raulbolivarnavas.supportlogging.adapter.SupportLogger;
import io.github.raulbolivarnavas.supportlogging.helper.BuildCurl;
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
    public BuildCurl buildCurl(
            ObjectMapper objectMapper
    ) {
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
            BuildCurl buildCurl
    ) {
        return new SupportLogger(
                properties,
                objectMapper,
                buildCurl
        );
    }
}
