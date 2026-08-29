package io.github.raulbolivarnavas.supportlogging.model;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Stores the current request and response in the Reactor context for later logging.
 */
public class SupportLogCapture {

    /**
     * Captures request metadata into the active {@link SupportLogState}.
     *
     * @param method the HTTP method
     * @param url the request URL
     * @param queryParams the query parameters
     * @param headers the request headers
     * @param body the request body
     * @return a completion signal that keeps the reactive chain intact
     */
    public Mono<Void> request(
            String method,
            String url,
            Map<String, String> queryParams,
            Map<String, String> headers,
            Object body
    ) {
        return Mono.deferContextual(context -> {
            SupportLogState state =
                    context.getOrDefault(SupportLogState.class, null);

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
     * Captures the response payload into the active {@link SupportLogState}.
     *
     * @param response the response payload
     * @param <T> the response type
     * @return a mono that emits the supplied response value
     */
    public <T> Mono<T> response(T response) {
        return Mono.deferContextual(context -> {
            SupportLogState state =
                    context.getOrDefault(SupportLogState.class, null);

            if (state != null) {
                state.setResponse(response);
            }

            return Mono.justOrEmpty(response);
        });
    }
}
