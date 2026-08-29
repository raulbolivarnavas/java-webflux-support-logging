package io.github.raulbolivarnavas.supportlogging.adapter;

import io.github.raulbolivarnavas.supportlogging.config.SupportLoggingConfiguration;
import io.github.raulbolivarnavas.supportlogging.helper.BuildCurl;
import io.github.raulbolivarnavas.supportlogging.json.JsonFormatter;
import io.github.raulbolivarnavas.supportlogging.masking.DataMasker;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Coordinates support log rendering and dispatches the final message to SLF4J.
 */
public class SupportLogger {

    private static final Logger log = LoggerFactory.getLogger(SupportLogger.class);

    private final SupportLoggingConfiguration configuration;
    private final JsonFormatter jsonFormatter;
    private final BuildCurl buildCurl;
    private final DataMasker dataMasker;

    /**
     * Creates a logger with the collaborators needed to format and mask log payloads.
     *
     * @param configuration resolved logging configuration
     * @param jsonFormatter JSON formatter used for structured output
     * @param buildCurl curl command builder used in debug logs
     * @param dataMasker masker applied to headers, query params, request and response
     */
    public SupportLogger(
            SupportLoggingConfiguration configuration,
            JsonFormatter jsonFormatter,
            BuildCurl buildCurl,
            DataMasker dataMasker
    ) {
        this.configuration = configuration;
        this.jsonFormatter = jsonFormatter;
        this.buildCurl = buildCurl;
        this.dataMasker = dataMasker;
    }

    /**
     * Logs the captured state using the configured level.
     *
     * @param state the state to log
     */
    public void log(SupportLogState state) {
        if (state == null) {
            return;
        }

        if (configuration.resolvedLevel() == SupportLogLevel.DEBUG) {
            logDebug(state);
            return;
        }

        logInfo(state);
    }

    private void logInfo(SupportLogState state) {
        MaskedState masked = mask(state);

        if (state.getError() == null) {
            log.info(
                    "[SUPPORT] operation={} method={} url={} queryParams={} headers={} request={} response={}",
                    state.getOperation(),
                    state.getMethod(),
                    state.getUrl(),
                    jsonFormatter.compact(masked.queryParams()),
                    jsonFormatter.compact(masked.headers()),
                    jsonFormatter.compact(masked.request()),
                    jsonFormatter.compact(masked.response())
            );
            return;
        }

        log.error(
                "[SUPPORT] operation={} method={} url={} queryParams={} headers={} request={} errorType={} errorMessage={}",
                state.getOperation(),
                state.getMethod(),
                state.getUrl(),
                jsonFormatter.compact(masked.queryParams()),
                jsonFormatter.compact(masked.headers()),
                jsonFormatter.compact(masked.request()),
                state.getError().getClass().getName(),
                state.getError().getMessage()
        );
    }

    private void logDebug(SupportLogState state) {
        MaskedState masked = mask(state);

        String curl = buildCurl.build(
                new BuildCurl.CurlRequest(
                        state.getMethod(),
                        state.getUrl(),
                        asStringMap(masked.queryParams()),
                        asStringMap(masked.headers()),
                        masked.request()
                )
        );

        if (state.getError() == null) {
            log.info(
                    """
                    
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
                    
                    - [REQUEST-BODY]
                    {}
                    ────────────────────── RESPONSE ─────────────────────────────────────────────────
                    {}
                    ────────────────────── CURL ─────────────────────────────────────────────────────
                    {}
                    ─────────────────────────────────────────────────────────────────────────────────
                    """,
                    state.getOperation(),
                    state.getMethod(),
                    state.getUrl(),
                    jsonFormatter.pretty(masked.queryParams()),
                    jsonFormatter.pretty(masked.headers()),
                    jsonFormatter.pretty(masked.request()),
                    jsonFormatter.pretty(masked.response()),
                    curl
            );
            return;
        }

        log.error(
                """
                
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
                
                - [REQUEST-BODY]
                {}
                ────────────────────── RESPONSE ─────────────────────────────────────────────────
                - [ERROR-TYPE]
                {}
                
                - [ERROR-MESSAGE]
                {}
                ────────────────────── CURL ─────────────────────────────────────────────────────
                {}
                ─────────────────────────────────────────────────────────────────────────────────
                """,
                state.getOperation(),
                state.getMethod(),
                state.getUrl(),
                jsonFormatter.pretty(masked.queryParams()),
                jsonFormatter.pretty(masked.headers()),
                jsonFormatter.pretty(masked.request()),
                state.getError().getClass().getName(),
                state.getError().getMessage(),
                curl
        );
    }

    private MaskedState mask(SupportLogState state) {
        return new MaskedState(
                dataMasker.mask(state.getQueryParams()),
                dataMasker.mask(state.getHeaders()),
                dataMasker.mask(state.getRequest()),
                dataMasker.mask(state.getResponse())
        );
    }

    private Map<String, String> asStringMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }

        Map<String, String> result = new LinkedHashMap<>();

        map.forEach((key, item) -> {
            if (key != null && item != null) {
                result.put(String.valueOf(key), String.valueOf(item));
            }
        });

        return result;
    }

    private record MaskedState(
            Object queryParams,
            Object headers,
            Object request,
            Object response
    ) {
    }
}
