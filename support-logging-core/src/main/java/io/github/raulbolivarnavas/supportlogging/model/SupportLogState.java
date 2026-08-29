package io.github.raulbolivarnavas.supportlogging.model;

/**
 * Mutable state collected during a reactive request and later rendered in the log entry.
 */
public class SupportLogState {

    private String operation;
    private String method;
    private String url;
    private Object queryParams;
    private Object headers;
    private Object request;
    private Object response;
    private Throwable error;

    /**
     * Returns the current operation name.
     *
     * @return the operation name
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets the operation name.
     *
     * @param operation the operation name
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Returns the HTTP method.
     *
     * @return the HTTP method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the HTTP method.
     *
     * @param method the HTTP method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Returns the request URL.
     *
     * @return the request URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the request URL.
     *
     * @param url the request URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the captured query parameters.
     *
     * @return the query parameters
     */
    public Object getQueryParams() {
        return queryParams;
    }

    /**
     * Sets the captured query parameters.
     *
     * @param queryParams the query parameters
     */
    public void setQueryParams(Object queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * Returns the captured headers.
     *
     * @return the headers
     */
    public Object getHeaders() {
        return headers;
    }

    /**
     * Sets the captured headers.
     *
     * @param headers the headers
     */
    public void setHeaders(Object headers) {
        this.headers = headers;
    }

    /**
     * Returns the captured request payload.
     *
     * @return the request payload
     */
    public Object getRequest() {
        return request;
    }

    /**
     * Sets the captured request payload.
     *
     * @param request the request payload
     */
    public void setRequest(Object request) {
        this.request = request;
    }

    /**
     * Returns the captured response payload.
     *
     * @return the response payload
     */
    public Object getResponse() {
        return response;
    }

    /**
     * Sets the captured response payload.
     *
     * @param response the response payload
     */
    public void setResponse(Object response) {
        this.response = response;
    }

    /**
     * Returns the captured error.
     *
     * @return the error, or {@code null} when the call completed successfully
     */
    public Throwable getError() {
        return error;
    }

    /**
     * Sets the captured error.
     *
     * @param error the error thrown during the reactive pipeline
     */
    public void setError(Throwable error) {
        this.error = error;
    }
}
