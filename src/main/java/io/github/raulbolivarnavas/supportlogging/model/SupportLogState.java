package io.github.raulbolivarnavas.supportlogging.model;

import lombok.*;

import java.util.Map;

/**
 * Represents the state of a support log entry, capturing all details of a request-response cycle.
 * This class holds information about the HTTP operation, request details, response, and any errors that occurred.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("javadoc")
public class SupportLogState {
    /**
     * The name of the operation being performed.
     */
    private String operation;
    /**
     * The HTTP method of the request (e.g., GET, POST, PUT, DELETE).
     */
    private String method;
    /**
     * The URL endpoint of the request.
     */
    private String url;
    /**
     * Query parameters included in the request URL.
     */
    private Map<String, String> queryParams;
    /**
     * HTTP headers sent with the request.
     */
    private Map<String, String> headers;
    /**
     * The request payload or body.
     */
    private Object request;
    /**
     * The response received from the HTTP call.
     */
    private Object response;
    /**
     * Any exception or error that occurred during the operation.
     */
    private Throwable error;
}