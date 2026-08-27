package io.github.raulbolivarnavas.supportlogging.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.raulbolivarnavas.supportlogging.config.SupportLoggingProperties;
import io.github.raulbolivarnavas.supportlogging.helper.BuildCurl;
import io.github.raulbolivarnavas.supportlogging.masking.DataMasker;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SupportLogger {

    private final SupportLoggingProperties properties;
    private final ObjectMapper objectMapper;
    private final BuildCurl buildCurl;
    private final DataMasker dataMasker;

    public void log(SupportLogState state) {

        if (properties.resolvedLevel() == SupportLogLevel.DEBUG) {
            logDebug(state);
            return;
        }

        logInfo(state);
    }

    private void logInfo(SupportLogState state) {

        Object queryParams =
                dataMasker.mask(state.getQueryParams());

        Object headers =
                dataMasker.mask(state.getHeaders());

        Object request =
                dataMasker.mask(state.getRequest());

        Object response =
                dataMasker.mask(state.getResponse());

        log.info(
                "[SUPPORT] operation={} method={} url={} queryParams={} headers={} request={} response={}",
                state.getOperation(),
                state.getMethod(),
                state.getUrl(),
                compactJson(queryParams),
                compactJson(headers),
                compactJson(request),
                compactJson(response)
        );
    }

    private void logDebug(SupportLogState state) {

        Object queryParams =
                dataMasker.mask(state.getQueryParams());

        Object headers =
                dataMasker.mask(state.getHeaders());

        Object request =
                dataMasker.mask(state.getRequest());

        Object response =
                dataMasker.mask(state.getResponse());

        String curl = buildCurl.build(
                BuildCurl.CurlRequest.builder()
                        .method(state.getMethod())
                        .url(state.getUrl())
                        .queryParams(asStringMap(queryParams))
                        .headers(asStringMap(headers))
                        .body(request)
                        .build()
        );

        log.info("""
            
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
            """,
                state.getOperation(),
                state.getMethod(),
                state.getUrl(),
                prettyJson(queryParams),
                prettyJson(headers),
                prettyJson(request),
                prettyJson(response),
                curl
        );
    }

    private Map<String, String> asStringMap(Object value) {

        if (value == null) {
            return Map.of();
        }

        return objectMapper.convertValue(
                value,
                objectMapper
                        .getTypeFactory()
                        .constructMapType(
                                LinkedHashMap.class,
                                String.class,
                                String.class
                        )
        );
    }

    private String compactJson(Object value) {

        if (value == null) {
            return "-";
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    private String prettyJson(Object value) {

        if (value == null) {
            return "-";
        }

        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}