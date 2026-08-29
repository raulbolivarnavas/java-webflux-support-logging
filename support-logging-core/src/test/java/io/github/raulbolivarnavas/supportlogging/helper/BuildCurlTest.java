package io.github.raulbolivarnavas.supportlogging.helper;

import io.github.raulbolivarnavas.supportlogging.json.JsonFormatter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildCurlTest {

    private static final JsonFormatter FORMATTER = new JsonFormatter() {
        @Override
        public String compact(Object value) {
            return String.valueOf(value);
        }

        @Override
        public String pretty(Object value) {
            return String.valueOf(value);
        }
    };

    @Test
    void buildShouldReturnEmptyStringWhenRequestIsNull() {
        BuildCurl buildCurl = new BuildCurl(FORMATTER);

        assertEquals("", buildCurl.build(null));
    }

    @Test
    void buildShouldRenderDefaultsQueryParamsHeadersAndBody() {
        BuildCurl buildCurl = new BuildCurl(FORMATTER);

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("name", "John Doe");
        queryParams.put("filter", "a&b");

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "O'Reilly");

        String curl = buildCurl.build(
                new BuildCurl.CurlRequest(
                        " ",
                        "https://example.com/users?existing=true",
                        queryParams,
                        headers,
                        Map.of("id", 1)
                )
        );

        assertTrue(curl.contains("-X 'GET'"));
        assertTrue(curl.contains(
                "'https://example.com/users?existing=true&name=John+Doe&filter=a%26b'"
        ));
        assertTrue(curl.contains("'Authorization: O'\"'\"'Reilly'"));
        assertTrue(curl.contains("--data-raw '{id=1}'"));
    }

    @Test
    void buildShouldIgnoreMissingOptionalValues() {
        BuildCurl buildCurl = new BuildCurl(FORMATTER);

        String curl = buildCurl.build(
                new BuildCurl.CurlRequest(
                        "POST",
                        "https://example.com/items",
                        null,
                        null,
                        null
                )
        );

        assertEquals("curl -X 'POST' 'https://example.com/items'", curl);
    }

    @Test
    void shellQuoteShouldHandleNullValue() throws Exception {
        BuildCurl buildCurl = new BuildCurl(FORMATTER);
        Method method = BuildCurl.class.getDeclaredMethod("shellQuote", String.class);
        method.setAccessible(true);

        assertEquals("''", method.invoke(buildCurl, new Object[]{null}));
    }
}
