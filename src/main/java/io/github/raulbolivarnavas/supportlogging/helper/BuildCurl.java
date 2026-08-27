package io.github.raulbolivarnavas.supportlogging.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper component for building cURL command representations of HTTP requests.
 * This utility is useful for debugging and logging HTTP interactions in a human-readable format.
 */
@RequiredArgsConstructor
@SuppressWarnings("javadoc")
public class BuildCurl {

    private static final String LINE_CONTINUATION = " \\\n";

    private final ObjectMapper objectMapper;

    /**
     * Record representing a cURL request with all necessary HTTP details.
     *
     * @param method the HTTP method (GET, POST, PUT, DELETE, etc.)
     * @param url the target URL
     * @param queryParams query parameters to append to the URL
     * @param headers HTTP headers to include in the request
     * @param body the request body/payload
     */
    @Builder
    public record CurlRequest(String method,
                              String url,
                              Map<String, String> queryParams,
                              Map<String, String> headers,
                              Object body) {
        /**
         * Custom builder for CurlRequest with convenience methods for setting query parameters and headers.
         */
        @SuppressWarnings("javadoc")
        public static class CurlRequestBuilder {

            /**
             * Sets query parameters from a variable array of key-value pairs.
             *
             * @param values pairs of strings representing key and value
             * @return this builder instance
             */
            public CurlRequestBuilder queryParams(String... values) {
                this.queryParams = toMap(values);
                return this;
            }

            /**
             * Sets query parameters from a map.
             *
             * @param values map of query parameter key-value pairs
             * @return this builder instance
             */
            public CurlRequestBuilder queryParams(Map<String, String> values) {
                this.queryParams = values != null
                        ? new LinkedHashMap<>(values)
                        : Map.of();

                return this;
            }

            /**
             * Sets headers from a variable array of key-value pairs.
             *
             * @param values pairs of strings representing header name and value
             * @return this builder instance
             */
            public CurlRequestBuilder headers(String... values) {
                this.headers = toMap(values);
                return this;
            }

            /**
             * Sets headers from a map.
             *
             * @param values map of header key-value pairs
             * @return this builder instance
             */
            public CurlRequestBuilder headers(Map<String, String> values) {
                this.headers = values != null
                        ? new LinkedHashMap<>(values)
                        : Map.of();

                return this;
            }

            /**
             * Converts a variable array of strings into a map of key-value pairs.
             *
             * @param values alternating key-value pairs
             * @return a map constructed from the pairs
             * @throws IllegalArgumentException if the number of values is odd
             */
            private static Map<String, String> toMap(String... values) {
                if (values == null || values.length == 0) {
                    return Map.of();
                }

                if (values.length % 2 != 0) {
                    throw new IllegalArgumentException(
                            "Values must be provided as name/value pairs"
                    );
                }

                Map<String, String> result = new LinkedHashMap<>();

                for (int i = 0; i < values.length; i += 2) {
                    if (values[i + 1] != null) {
                        result.put(values[i], values[i + 1]);
                    }
                }

                return result;
            }
        }
    }

    /**
     * Builds a complete cURL command string from the provided request details.
     *
     * @param request the CurlRequest containing all HTTP details
     * @return a string representation of the cURL command
     */
    public String build(CurlRequest request) {
        StringBuilder curl = new StringBuilder("curl --location");

        appendMethod(curl, request.method());

        curl.append(" '")
                .append(buildUrl(request))
                .append("'");

        Map<String, String> headers = new LinkedHashMap<>();

        if (request.headers() != null) {
            headers.putAll(request.headers());
        }

        if (request.body() != null) {
            headers.putIfAbsent("Content-Type", "application/json");
        }

        appendHeaders(curl, headers);
        appendBody(curl, request.body());

        return curl.toString();
    }

    /**
     * Appends the HTTP method to the cURL command if provided.
     *
     * @param curl the StringBuilder to append to
     * @param method the HTTP method
     */
    private void appendMethod(StringBuilder curl, String method) {
        if (method != null && !method.isBlank()) {
            curl.append(" --request ")
                    .append(method.toUpperCase());
        }
    }

    /**
     * Builds the complete URL with query parameters if present.
     *
     * @param request the CurlRequest containing URL and query parameters
     * @return the URL with appended query string if applicable
     */
    private String buildUrl(CurlRequest request) {
        if (request.queryParams() == null || request.queryParams().isEmpty()) {
            return request.url();
        }

        String query = request.queryParams().entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return query.isBlank()
                ? request.url()
                : request.url() + "?" + query;
    }

    /**
     * Appends HTTP headers to the cURL command.
     *
     * @param curl the StringBuilder to append to
     * @param headers the map of header key-value pairs
     */
    private void appendHeaders(StringBuilder curl, Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }

        headers.forEach((key, value) -> {
            if (value != null) {
                curl.append(LINE_CONTINUATION)
                        .append("--header '")
                        .append(key)
                        .append(": ")
                        .append(value)
                        .append("'");
            }
        });
    }

    /**
     * Appends the request body to the cURL command if present.
     *
     * @param curl the StringBuilder to append to
     * @param body the request body object
     */
    private void appendBody(StringBuilder curl, Object body) {
        if (body == null) {
            return;
        }

        curl.append(LINE_CONTINUATION)
                .append("--data-raw '")
                .append(toJson(body))
                .append("'");
    }

    /**
     * Converts the request body object to a JSON string representation.
     *
     * @param body the object to serialize
     * @return JSON string representation of the body
     * @throws IllegalArgumentException if JSON serialization fails
     */
    private String toJson(Object body) {
        if (body instanceof String value) {
            return value;
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize CURL body", e);
        }
    }
}