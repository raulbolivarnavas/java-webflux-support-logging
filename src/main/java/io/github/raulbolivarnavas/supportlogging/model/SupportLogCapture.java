package io.github.raulbolivarnavas.supportlogging.model;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Component responsible for capturing support log information during the execution of requests and responses in a reactive context.
 * This component provides methods to capture request details (method, URL, query parameters, headers, and body)
 * and response details. The captured information is stored in a SupportLogState object accessible from the reactive context.
 */
@Component
@SuppressWarnings("javadoc")
public class SupportLogCapture {

    /**
     * Captures the HTTP request details and stores them in the SupportLogState object within the reactive context.
     *
     * @param method The HTTP method of the request (e.g., GET, POST, PUT, DELETE).
     * @param url The URL endpoint of the request.
     * @param queryParams The query parameters of the request as a map of key-value pairs.
     * @param headers The HTTP headers of the request as a map of key-value pairs.
     * @param body The request body or payload.
     * @return A Mono that completes when the request details have been captured.
     */
    public Mono<Void> request(
            String method,
            String url,
            Map<String, String> queryParams,
            Map<String, String> headers,
            Object body) {

        return Mono.deferContextual(context -> {
            SupportLogState state = context.getOrDefault(SupportLogState.class, null);

            if (state != null) {
                state.setMethod(method);
                state.setUrl(url);
                state.setQueryParams(queryParams);
                state.setHeaders(headers);
                state.setRequest(body);
            }

            return Mono.empty();
        });
    }

    /**
     * Captures the HTTP response details and stores them in the SupportLogState object within the reactive context.
     *
     * @param response The response object received from the HTTP call.
     * @param <T> The type parameter representing the response type.
     * @return A Mono that emits the response after it has been captured.
     */
    public <T> Mono<T> response(T response) {
        return Mono.deferContextual(context -> {
            SupportLogState state = context.getOrDefault(SupportLogState.class, null);

            if (state != null) {
                state.setResponse(response);
            }

            return Mono.just(response);
        });
    }
}