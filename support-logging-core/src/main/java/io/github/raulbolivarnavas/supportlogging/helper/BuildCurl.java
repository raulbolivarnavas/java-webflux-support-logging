package io.github.raulbolivarnavas.supportlogging.helper;

import io.github.raulbolivarnavas.supportlogging.json.JsonFormatter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Builds a shell-safe cURL command from the captured request state.
 */
public class BuildCurl {

    private final JsonFormatter jsonFormatter;

    /**
     * Creates a new builder.
     *
     * @param jsonFormatter formatter used to serialize the request body
     */
    public BuildCurl(JsonFormatter jsonFormatter) {
        this.jsonFormatter = jsonFormatter;
    }

    /**
     * Builds a cURL command for the supplied request snapshot.
     *
     * @param request the request to convert
     * @return a shell-quoted cURL command, or an empty string when request is null
     */
    public String build(CurlRequest request) {
        if (request == null) {
            return "";
        }

        StringBuilder curl = new StringBuilder("curl");

        String method = blankToDefault(request.method(), "GET");
        curl.append(" -X ").append(shellQuote(method));

        String finalUrl = appendQueryParams(
                blankToDefault(request.url(), ""),
                request.queryParams()
        );

        curl.append(" ").append(shellQuote(finalUrl));

        if (request.headers() != null) {
            request.headers().forEach((key, value) -> {
                if (key != null && value != null) {
                    curl.append(" -H ")
                            .append(shellQuote(key + ": " + value));
                }
            });
        }

        if (request.body() != null) {
            curl.append(" --data-raw ")
                    .append(shellQuote(jsonFormatter.compact(request.body())));
        }

        return curl.toString();
    }

    private String appendQueryParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }

        StringJoiner joiner = new StringJoiner("&");

        queryParams.forEach((key, value) -> {
            if (key != null && value != null) {
                joiner.add(encode(key) + "=" + encode(value));
            }
        });

        String query = joiner.toString();

        if (query.isBlank()) {
            return url;
        }

        return url + (url.contains("?") ? "&" : "?") + query;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String shellQuote(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * Immutable request snapshot used to render a cURL command.
     */
    public record CurlRequest(
            String method,
            String url,
            Map<String, String> queryParams,
            Map<String, String> headers,
            Object body
    ) {
    }
}
