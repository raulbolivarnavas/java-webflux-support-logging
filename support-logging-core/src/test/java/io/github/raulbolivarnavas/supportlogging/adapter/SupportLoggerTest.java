package io.github.raulbolivarnavas.supportlogging.adapter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.raulbolivarnavas.supportlogging.config.SupportLoggingConfiguration;
import io.github.raulbolivarnavas.supportlogging.helper.BuildCurl;
import io.github.raulbolivarnavas.supportlogging.json.JsonFormatter;
import io.github.raulbolivarnavas.supportlogging.masking.DataMasker;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportLoggerTest {

    private final MutableConfiguration configuration = new MutableConfiguration();
    private final JsonFormatter jsonFormatter = new TestJsonFormatter();
    private final DataMasker dataMasker = value -> value;
    private final BuildCurl buildCurl = new BuildCurl(jsonFormatter);

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(SupportLogger.class);
        logger.setLevel(Level.INFO);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void logShouldIgnoreNullState() {
        new SupportLogger(configuration, jsonFormatter, buildCurl, dataMasker).log(null);

        assertFalse(hasSupportMessage());
    }

    @Test
    void logShouldRenderInfoMessageWithoutError() {
        configuration.level = SupportLogLevel.INFO;

        new SupportLogger(configuration, jsonFormatter, buildCurl, dataMasker).log(createState(null));

        String message = firstMessage();
        assertTrue(message.contains("[SUPPORT] operation=operation"));
        assertTrue(message.contains("queryParams={id=1}"));
        assertTrue(message.contains("headers={Accept=application/json}"));
        assertTrue(message.contains("request={body=payload}"));
        assertTrue(message.contains("response={status=ok}"));
    }

    @Test
    void logShouldRenderErrorMessageWhenLevelIsInfo() {
        configuration.level = SupportLogLevel.INFO;

        new SupportLogger(configuration, jsonFormatter, buildCurl, dataMasker).log(
                createState(new IllegalStateException("boom"))
        );

        String message = firstMessage();
        assertTrue(message.contains("errorType=java.lang.IllegalStateException"));
        assertTrue(message.contains("errorMessage=boom"));
    }

    @Test
    void logShouldRenderDebugMessageAndBuildCurl() {
        configuration.level = SupportLogLevel.DEBUG;

        new SupportLogger(configuration, jsonFormatter, buildCurl, dataMasker).log(createState(null));

        String message = firstMessage();
        assertTrue(message.contains("## [SUPPORT HTTP CALL] ##"));
        assertTrue(message.contains("curl -X 'POST' 'https://example.com/items?id=1'"));
    }

    @Test
    void logShouldRenderDebugErrorMessage() {
        configuration.level = SupportLogLevel.DEBUG;

        new SupportLogger(configuration, jsonFormatter, buildCurl, dataMasker).log(
                createState(new IllegalStateException("boom"))
        );

        String message = firstMessage();
        assertTrue(message.contains("- [ERROR-TYPE]"));
        assertTrue(message.contains("java.lang.IllegalStateException"));
        assertTrue(message.contains("boom"));
    }

    private SupportLogState createState(Throwable error) {
        SupportLogState state = new SupportLogState();
        state.setOperation("operation");
        state.setMethod("POST");
        state.setUrl("https://example.com/items");
        state.setQueryParams(Map.of("id", "1"));
        state.setHeaders(Map.of("Accept", "application/json"));
        state.setRequest(Map.of("body", "payload"));
        state.setResponse(Map.of("status", "ok"));
        state.setError(error);
        return state;
    }

    private boolean hasSupportMessage() {
        return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("[SUPPORT]"));
    }

    private String firstMessage() {
        return appender.list.get(0).getFormattedMessage();
    }

    private static final class MutableConfiguration implements SupportLoggingConfiguration {
        private SupportLogLevel level = SupportLogLevel.INFO;

        @Override
        public SupportLogLevel resolvedLevel() {
            return level;
        }
    }

    private static final class TestJsonFormatter implements JsonFormatter {
        @Override
        public String compact(Object value) {
            return String.valueOf((Object) value);
        }

        @Override
        public String pretty(Object value) {
            return String.valueOf((Object) value);
        }
    }
}
