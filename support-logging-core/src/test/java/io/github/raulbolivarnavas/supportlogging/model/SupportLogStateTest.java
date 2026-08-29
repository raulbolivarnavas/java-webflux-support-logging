package io.github.raulbolivarnavas.supportlogging.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SupportLogStateTest {

    @Test
    void shouldStoreAllCapturedValues() {
        Throwable error = new IllegalStateException("boom");
        SupportLogState state = new SupportLogState();

        state.setOperation("operation");
        state.setMethod("POST");
        state.setUrl("https://example.com");
        state.setQueryParams("query");
        state.setHeaders("headers");
        state.setRequest("request");
        state.setResponse("response");
        state.setError(error);

        assertEquals("operation", state.getOperation());
        assertEquals("POST", state.getMethod());
        assertEquals("https://example.com", state.getUrl());
        assertEquals("query", state.getQueryParams());
        assertEquals("headers", state.getHeaders());
        assertEquals("request", state.getRequest());
        assertEquals("response", state.getResponse());
        assertEquals(error, state.getError());
    }

    @Test
    void shouldDefaultToNullValues() {
        SupportLogState state = new SupportLogState();

        assertNull(state.getOperation());
        assertNull(state.getMethod());
        assertNull(state.getUrl());
        assertNull(state.getQueryParams());
        assertNull(state.getHeaders());
        assertNull(state.getRequest());
        assertNull(state.getResponse());
        assertNull(state.getError());
    }
}
