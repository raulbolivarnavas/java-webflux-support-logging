package io.github.raulbolivarnavas.supportlogging.model;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupportLogCaptureTest {

    private final SupportLogCapture capture = new SupportLogCapture();

    @Test
    void requestShouldCaptureValuesInContext() {
        SupportLogState state = new SupportLogState();

        StepVerifier.create(
                capture.request(
                        "POST",
                        "https://example.com/items",
                        Map.of("id", "1"),
                        Map.of("Accept", "application/json"),
                        Map.of("body", "payload")
                ).contextWrite(Context.of(SupportLogState.class, state))
        ).verifyComplete();

        assertEquals("POST", state.getMethod());
        assertEquals("https://example.com/items", state.getUrl());
        assertEquals(Map.of("id", "1"), state.getQueryParams());
        assertEquals(Map.of("Accept", "application/json"), state.getHeaders());
        assertEquals(Map.of("body", "payload"), state.getRequest());
    }

    @Test
    void responseShouldCaptureValuesInContext() {
        SupportLogState state = new SupportLogState();

        StepVerifier.create(
                capture.response("payload")
                        .contextWrite(Context.of(SupportLogState.class, state))
        ).expectNext("payload").verifyComplete();

        assertEquals("payload", state.getResponse());
    }

    @Test
    void shouldCompleteWithoutContext() {
        StepVerifier.create(capture.request("GET", "/", null, null, null))
                .verifyComplete();

        StepVerifier.create(capture.response(null)).verifyComplete();
    }
}
