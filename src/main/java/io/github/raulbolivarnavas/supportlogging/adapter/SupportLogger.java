package io.github.raulbolivarnavas.supportlogging.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.raulbolivarnavas.supportlogging.helper.BuildCurl;
import io.github.raulbolivarnavas.supportlogging.config.SupportLoggingProperties;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SupportLogger is a component responsible for logging support-related information in a structured format.
 * It provides methods to log support operations, including request and response details, in both compact and pretty JSON formats.
 * The logging level can be configured through the SupportLoggingProperties, allowing for different levels of verbosity in the logs.
 * <p>
 * The logger supports two logging levels: INFO and DEBUG. In DEBUG mode, it logs detailed information, including the cURL command for the HTTP request.
 * <p>
 * The logger uses the Jackson ObjectMapper for JSON serialization and handles potential serialization errors gracefully.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("javadoc")
public class SupportLogger {

    private static final String EMPTY_VALUE    = "-";
    private static final String DEBUG_TEMPLATE = """

            ──────────────────────────────────────────────────────────────────────────────────
             ## [SUPPORT HTTP CALL] ##
            ──────────────────────────────────────────────────────────────────────────────────
            - [OPERATION] : {}
            - [METHOD]    : {}
            - [URL]       : {}
            ────────────────────── REQUEST ───────────────────────────────────────────────────
            - [QUERY-PARAMS]
            {}

            - [HEADERS]
            {}

            - [BODY]
            {}
            ────────────────────── RESPONSE ─────────────────────────────────────────────────
            {}
            ────────────────────── CURL ─────────────────────────────────────────────────────
            {}
            ─────────────────────────────────────────────────────────────────────────────────
            """;

    private final SupportLoggingProperties properties;
    private final ObjectMapper             objectMapper;
    private final BuildCurl                buildCurl;

    /**
     * Logs the support operation details based on the provided SupportLogState.
     * The logging level is determined by the resolved level in SupportLoggingProperties.
     * @param state The SupportLogState containing the details of the support operation to be logged. If null, a warning is logged instead.
     */
    public void log(SupportLogState state) {
        if (state == null) {
            log.warn("[SUPPORT] SupportLogState is null");
            return;
        }

        if (properties.resolvedLevel() == SupportLogLevel.DEBUG) {
            logDebug(state);
            return;
        }

        logInfo(state);
    }

    /**
     * Logs the support operation details at the INFO level.
     * @param state The SupportLogState containing the details of the support operation to be logged.
     */
    private void logInfo(SupportLogState state) {
        log.info(
                "[SUPPORT] operation={} method={} url={} queryParams={} headers={} request={} response={}",
                value(state.getOperation()),
                value(state.getMethod()),
                value(state.getUrl()),
                compactJson(state.getQueryParams()),
                compactJson(state.getHeaders()),
                compactJson(state.getRequest()),
                compactJson(state.getResponse())
        );
    }

    /**
     * Logs the support operation details at the DEBUG level, including a cURL command representation of the HTTP request.
     * @param state The SupportLogState containing the details of the support operation to be logged.
     */
    private void logDebug(SupportLogState state) {

        String curl = buildCurl.build(
                BuildCurl.CurlRequest.builder()
                        .method(state.getMethod())
                        .url(state.getUrl())
                        .queryParams(state.getQueryParams())
                        .headers(state.getHeaders())
                        .body(state.getRequest())
                        .build()
        );

        log.info(
                DEBUG_TEMPLATE,
                value(state.getOperation()),
                value(state.getMethod()),
                value(state.getUrl()),
                prettyJson(state.getQueryParams()),
                prettyJson(state.getHeaders()),
                prettyJson(state.getRequest()),
                prettyJson(state.getResponse()),
                value(curl)
        );
    }

    /**
     * Serializes the given value into a compact JSON string representation.
     * If the value is null, it returns a predefined EMPTY_VALUE.
     * If serialization fails, it logs the error and returns the string representation of the value.
     * @param value The object to be serialized into JSON.
     * @return A compact JSON string representation of the value, or EMPTY_VALUE if the value is null, or the string representation of the value if serialization fails.
     */
    private String compactJson(Object value) {
        if (value == null) {
            return EMPTY_VALUE;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.trace("[SUPPORT] Unable to serialize value as compact JSON: {}",
                    e.getMessage()
            );

            return String.valueOf(value);
        }
    }

    /**
     * Serializes the given value into a pretty-printed JSON string representation.
     * If the value is null, it returns a predefined EMPTY_VALUE.
     * If serialization fails, it logs the error and returns the string representation of the value.
     * @param value The object to be serialized into pretty-printed JSON.
     * @return A pretty-printed JSON string representation of the value, or EMPTY_VALUE if the value is null, or the string representation of the value if serialization fails.
     */
    private String prettyJson(Object value) {
        if (value == null) {
            return EMPTY_VALUE;
        }

        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.trace("[SUPPORT] Unable to serialize value as pretty JSON: {}",
                    e.getMessage()
            );

            return String.valueOf(value);
        }
    }

    private String value(String value) {
        return value == null || value.isBlank()
                ? EMPTY_VALUE
                : value;
    }
}