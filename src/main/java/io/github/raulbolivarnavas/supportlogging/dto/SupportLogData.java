package io.github.raulbolivarnavas.supportlogging.dto;

import lombok.Builder;

import java.util.Map;

/**
 * Record that holds information about a support log entry.
 * This immutable data structure captures all relevant details of an HTTP operation for logging purposes.
 *
 * @param operation The operation name being performed.
 * @param method The HTTP method used (e.g., GET, POST, PUT, DELETE).
 * @param url The URL endpoint being accessed.
 * @param headers The HTTP headers sent with the request.
 * @param request The request payload or body.
 * @param response The response payload received.
 * @param error The error or exception encountered, if any.
 */
@Builder
public record SupportLogData(
        String operation,
        String method,
        String url,
        Map<String, String> headers,
        Object request,
        Object response,
        Throwable error
) {
}