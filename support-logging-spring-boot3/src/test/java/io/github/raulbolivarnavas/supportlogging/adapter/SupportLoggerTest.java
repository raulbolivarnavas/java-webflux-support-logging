package io.github.raulbolivarnavas.supportlogging.adapter;

import io.github.raulbolivarnavas.supportlogging.config.SupportLoggingConfiguration;
import io.github.raulbolivarnavas.supportlogging.helper.BuildCurl;
import io.github.raulbolivarnavas.supportlogging.json.JsonFormatter;
import io.github.raulbolivarnavas.supportlogging.masking.DataMasker;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class SupportLoggerTest {

    private final SupportLoggingConfiguration configuration = mock(SupportLoggingConfiguration.class);
    private final JsonFormatter jsonFormatter = mock(JsonFormatter.class);
    private final BuildCurl buildCurl = mock(BuildCurl.class);
    private final DataMasker dataMasker = mock(DataMasker.class);

    @Test
    void logShouldIgnoreNullState(CapturedOutput output) {
        new SupportLogger(configuration, jsonFormatter, buildCurl, dataMasker).log(null);

        assertFalse((output.getOut() + output.getErr()).contains("[SUPPORT]"));
    }

    @Test
    void logShouldRenderInfoMessageWithoutError(CapturedOutput output) {
        when(configuration.resolvedLevel()).thenReturn(SupportLogLevel.INFO);
        when(dataMasker.mask(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jsonFormatter.compact(any())).thenAnswer(
                invocation -> String.valueOf((Object) invocation.getArgument(0))
        );

        SupportLogState state = createState();

        new SupportLogger(configuration, jsonFormatter, buildCurl, dataMasker).log(state);

        String text = output.getOut() + output.getErr();
        assertTrue(text.contains("[SUPPORT] operation=operation"));
        assertTrue(text.contains("queryParams={id=1}"));
        assertTrue(text.contains("request={body=payload}"));
    }

    @Test
    void logShouldRenderErrorMessageWhenLevelIsInfo(CapturedOutput output) {
        when(configuration.resolvedLevel()).thenReturn(SupportLogLevel.INFO);
        when(dataMasker.mask(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jsonFormatter.compact(any())).thenAnswer(
                invocation -> String.valueOf((Object) invocation.getArgument(0))
        );

        SupportLogState state = createState();
        state.setError(new IllegalStateException("boom"));

        new SupportLogger(configuration, jsonFormatter, buildCurl, dataMasker).log(state);

        String text = output.getOut() + output.getErr();
        assertTrue(text.contains("errorType=java.lang.IllegalStateException"));
        assertTrue(text.contains("errorMessage=boom"));
    }

    @Test
    void logShouldRenderDebugMessageAndBuildCurl(CapturedOutput output) {
        when(configuration.resolvedLevel()).thenReturn(SupportLogLevel.DEBUG);
        when(dataMasker.mask(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jsonFormatter.pretty(any())).thenAnswer(
                invocation -> String.valueOf((Object) invocation.getArgument(0))
        );
        when(buildCurl.build(any())).thenReturn("curl -X 'POST' 'https://example.com/items'");

        SupportLogState state = createState();

        new SupportLogger(configuration, jsonFormatter, buildCurl, dataMasker).log(state);

        ArgumentCaptor<BuildCurl.CurlRequest> captor =
                ArgumentCaptor.forClass(BuildCurl.CurlRequest.class);
        verify(buildCurl).build(captor.capture());

        BuildCurl.CurlRequest request = captor.getValue();
        assertEquals("POST", request.method());
        assertEquals("https://example.com/items", request.url());
        assertEquals(Map.of("id", "1"), request.queryParams());
        assertEquals(Map.of("Accept", "application/json"), request.headers());
        assertEquals(Map.of("body", "payload"), request.body());

        String text = output.getOut() + output.getErr();
        assertTrue(text.contains("## [SUPPORT HTTP CALL] ##"));
        assertTrue(text.contains("curl -X 'POST' 'https://example.com/items'"));
    }

    @Test
    void logShouldRenderDebugErrorMessage(CapturedOutput output) {
        when(configuration.resolvedLevel()).thenReturn(SupportLogLevel.DEBUG);
        when(dataMasker.mask(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jsonFormatter.pretty(any())).thenAnswer(
                invocation -> String.valueOf((Object) invocation.getArgument(0))
        );
        when(buildCurl.build(any())).thenReturn("curl -X 'POST' 'https://example.com/items'");

        SupportLogState state = createState();
        state.setError(new IllegalStateException("boom"));

        new SupportLogger(configuration, jsonFormatter, buildCurl, dataMasker).log(state);

        String text = output.getOut() + output.getErr();
        assertTrue(text.contains("ERROR-TYPE"));
        assertTrue(text.contains("java.lang.IllegalStateException"));
        assertTrue(text.contains("curl -X 'POST' 'https://example.com/items'"));
    }

    private SupportLogState createState() {
        SupportLogState state = new SupportLogState();
        state.setOperation("operation");
        state.setMethod("POST");
        state.setUrl("https://example.com/items");
        state.setQueryParams(Map.of("id", "1"));
        state.setHeaders(Map.of("Accept", "application/json"));
        state.setRequest(Map.of("body", "payload"));
        state.setResponse(Map.of("status", "ok"));
        return state;
    }
}
