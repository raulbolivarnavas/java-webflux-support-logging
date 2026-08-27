package io.github.raulbolivarnavas.supportlogging.adapter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.github.raulbolivarnavas.supportlogging.helper.BuildCurl;
import io.github.raulbolivarnavas.supportlogging.config.SupportLoggingProperties;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogLevel;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogState;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportLoggerTest {

    @Mock
    private SupportLoggingProperties properties;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private BuildCurl buildCurl;

    private SupportLogger supportLogger;
    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        supportLogger = new SupportLogger(properties, objectMapper, buildCurl);

        logger = (Logger) LoggerFactory.getLogger(SupportLogger.class);
        listAppender = new ListAppender<>();
        listAppender.start();

        logger.addAppender(listAppender);
        logger.setLevel(Level.INFO);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Nested
    @DisplayName("INFO logging")
    class InfoLogging {

        @Test
        @DisplayName("Debe generar log compacto cuando el nivel configurado es INFO")
        void shouldLogCompactInformationWhenLevelIsInfo() throws Exception {
            SupportLogState state = mockState();

            when(properties.resolvedLevel()).thenReturn(SupportLogLevel.INFO);
            when(objectMapper.writeValueAsString(state.getQueryParams()))
                    .thenReturn("{\"page\":\"1\"}");
            when(objectMapper.writeValueAsString(state.getHeaders()))
                    .thenReturn("{\"Authorization\":\"Bearer token\"}");
            when(objectMapper.writeValueAsString(state.getRequest()))
                    .thenReturn("{\"name\":\"Raul\"}");
            when(objectMapper.writeValueAsString(state.getResponse()))
                    .thenReturn("{\"status\":\"OK\"}");

            supportLogger.log(state);

            assertThat(listAppender.list).hasSize(1);

            ILoggingEvent event = listAppender.list.getFirst();

            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage())
                    .isEqualTo(
                            "[SUPPORT] operation=retrieve-card " +
                                    "method=POST " +
                                    "url=https://service.test/cards " +
                                    "queryParams={\"page\":\"1\"} " +
                                    "headers={\"Authorization\":\"Bearer token\"} " +
                                    "request={\"name\":\"Raul\"} " +
                                    "response={\"status\":\"OK\"}"
                    );

            verify(buildCurl, never()).build(any());
        }

        @Test
        @DisplayName("Debe representar valores null con guion")
        void shouldRepresentNullValuesWithDash() {
            SupportLogState state = SupportLogState.builder()
                    .operation("retrieve-card")
                    .method("GET")
                    .url("https://service.test/cards")
                    .queryParams(null)
                    .headers(null)
                    .request(null)
                    .response(null)
                    .build();

            when(properties.resolvedLevel()).thenReturn(SupportLogLevel.INFO);

            supportLogger.log(state);

            assertThat(listAppender.list).hasSize(1);
            assertThat(listAppender.list.getFirst().getFormattedMessage())
                    .isEqualTo(
                            "[SUPPORT] operation=retrieve-card " +
                                    "method=GET " +
                                    "url=https://service.test/cards " +
                                    "queryParams=- headers=- request=- response=-"
                    );

            verifyNoInteractions(objectMapper);
            verifyNoInteractions(buildCurl);
        }

        @Test
        @DisplayName("Debe usar String.valueOf cuando Jackson falla serializando JSON compacto")
        void shouldFallbackToStringValueWhenCompactSerializationFails() throws Exception {
            Map<String, String> queryParams = Map.of("page", "1");

            SupportLogState state = SupportLogState.builder()
                    .operation("retrieve-card")
                    .method("GET")
                    .url("https://service.test/cards")
                    .queryParams(queryParams)
                    .headers(null)
                    .request(null)
                    .response(null)
                    .build();

            when(properties.resolvedLevel()).thenReturn(SupportLogLevel.INFO);
            when(objectMapper.writeValueAsString(queryParams))
                    .thenThrow(mock(JsonProcessingException.class));

            supportLogger.log(state);

            assertThat(listAppender.list).hasSize(1);
            assertThat(listAppender.list.getFirst().getFormattedMessage())
                    .contains("queryParams={page=1}")
                    .contains("headers=-")
                    .contains("request=-")
                    .contains("response=-");

            verify(buildCurl, never()).build(any());
        }

        @Test
        @DisplayName("Debe manejar individualmente errores de serialización")
        void shouldHandleSerializationFailureIndependentlyForEveryField() throws Exception {
            Map<String, String> queryParams = Map.of("page", "1");
            Map<String, String> headers = Map.of("channel", "WEB");
            Object request = Map.of("id", "100");
            Object response = Map.of("result", "OK");

            SupportLogState state = SupportLogState.builder()
                    .operation("operation")
                    .method("POST")
                    .url("https://test")
                    .queryParams(queryParams)
                    .headers(headers)
                    .request(request)
                    .response(response)
                    .build();

            when(properties.resolvedLevel()).thenReturn(SupportLogLevel.INFO);

            when(objectMapper.writeValueAsString(queryParams))
                    .thenThrow(mock(JsonProcessingException.class));
            when(objectMapper.writeValueAsString(headers))
                    .thenReturn("{\"channel\":\"WEB\"}");
            when(objectMapper.writeValueAsString(request))
                    .thenThrow(mock(JsonProcessingException.class));
            when(objectMapper.writeValueAsString(response))
                    .thenReturn("{\"result\":\"OK\"}");

            supportLogger.log(state);

            String message = listAppender.list.getFirst().getFormattedMessage();

            assertThat(message)
                    .contains("queryParams={page=1}")
                    .contains("headers={\"channel\":\"WEB\"}")
                    .contains("request={id=100}")
                    .contains("response={\"result\":\"OK\"}");
        }
    }

    @Nested
    @DisplayName("DEBUG logging")
    class DebugLogging {

        @Test
        @DisplayName("Debe generar log detallado y construir CURL cuando el nivel es DEBUG")
        void shouldLogPrettyInformationAndCurlWhenLevelIsDebug() throws Exception {
            SupportLogState state = mockState();
            ObjectWriter writer = mock(ObjectWriter.class);

            when(properties.resolvedLevel()).thenReturn(SupportLogLevel.DEBUG);

            when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
            when(writer.writeValueAsString(state.getQueryParams()))
                    .thenReturn("""
                            {
                              "page" : "1"
                            }""");
            when(writer.writeValueAsString(state.getHeaders()))
                    .thenReturn("""
                            {
                              "Authorization" : "Bearer token"
                            }""");
            when(writer.writeValueAsString(state.getRequest()))
                    .thenReturn("""
                            {
                              "name" : "Raul"
                            }""");
            when(writer.writeValueAsString(state.getResponse()))
                    .thenReturn("""
                            {
                              "status" : "OK"
                            }""");

            when(buildCurl.build(any(BuildCurl.CurlRequest.class)))
                    .thenReturn("curl -X POST https://service.test/cards");

            supportLogger.log(state);

            assertThat(listAppender.list).hasSize(1);

            String message = listAppender.list.getFirst().getFormattedMessage();

            assertThat(message)
                    .contains("[SUPPORT HTTP CALL]")
                    .contains("[OPERATION] : retrieve-card")
                    .contains("[METHOD]    : POST")
                    .contains("[URL]       : https://service.test/cards")
                    .contains("\"page\" : \"1\"")
                    .contains("\"Authorization\" : \"Bearer token\"")
                    .contains("\"name\" : \"Raul\"")
                    .contains("\"status\" : \"OK\"")
                    .contains("curl -X POST https://service.test/cards");

            verify(buildCurl).build(any(BuildCurl.CurlRequest.class));
            verify(objectMapper, times(4)).writerWithDefaultPrettyPrinter();
            verify(objectMapper, never()).writeValueAsString(any());
        }

        @Test
        @DisplayName("Debe enviar al BuildCurl los datos correctos del estado")
        void shouldBuildCurlUsingStateValues() throws Exception {
            SupportLogState state = mockState();
            ObjectWriter writer = mock(ObjectWriter.class);

            when(properties.resolvedLevel()).thenReturn(SupportLogLevel.DEBUG);
            when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
            when(writer.writeValueAsString(any())).thenReturn("{}");

            when(buildCurl.build(any(BuildCurl.CurlRequest.class)))
                    .thenAnswer(invocation -> {
                        BuildCurl.CurlRequest request = invocation.getArgument(0);

                        assertThat(request.method()).isEqualTo("POST");
                        assertThat(request.url()).isEqualTo("https://service.test/cards");
                        assertThat(request.queryParams())
                                .isEqualTo(Map.of("page", "1"));
                        assertThat(request.headers())
                                .isEqualTo(Map.of("Authorization", "Bearer token"));
                        assertThat(request.body())
                                .isEqualTo(Map.of("name", "Raul"));

                        return "curl";
                    });

            supportLogger.log(state);

            verify(buildCurl).build(any(BuildCurl.CurlRequest.class));
        }

        @Test
        @DisplayName("Debe representar valores null con guion en DEBUG")
        void shouldRepresentNullValuesWithDashInDebug() {
            SupportLogState state = SupportLogState.builder()
                    .operation("retrieve-card")
                    .method("GET")
                    .url("https://service.test/cards")
                    .queryParams(null)
                    .headers(null)
                    .request(null)
                    .response(null)
                    .build();

            when(properties.resolvedLevel()).thenReturn(SupportLogLevel.DEBUG);
            when(buildCurl.build(any(BuildCurl.CurlRequest.class)))
                    .thenReturn("curl https://service.test/cards");

            supportLogger.log(state);

            assertThat(listAppender.list).hasSize(1);

            String message = listAppender.list.getFirst().getFormattedMessage();

            assertThat(message)
                    .contains("[SUPPORT HTTP CALL]")
                    .contains("[QUERY-PARAMS]\n-")
                    .contains("[HEADERS]\n-")
                    .contains("[BODY]\n-")
                    .contains("────────────────────── RESPONSE ─────────────────────────────────────────────────\n-")
                    .contains("────────────────────── CURL ─────────────────────────────────────────────────────\ncurl https://service.test/cards");

            verifyNoInteractions(objectMapper);
        }

        @Test
        @DisplayName("Debe usar String.valueOf cuando pretty printing falla")
        void shouldFallbackToStringValueWhenPrettySerializationFails() throws Exception {
            Map<String, String> request = Map.of("name", "Raul");

            SupportLogState state = SupportLogState.builder()
                    .operation("create-user")
                    .method("POST")
                    .url("https://service.test/users")
                    .queryParams(null)
                    .headers(null)
                    .request(request)
                    .response(null)
                    .build();

            ObjectWriter writer = mock(ObjectWriter.class);

            when(properties.resolvedLevel()).thenReturn(SupportLogLevel.DEBUG);
            when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
            when(writer.writeValueAsString(request))
                    .thenThrow(mock(JsonProcessingException.class));
            when(buildCurl.build(any(BuildCurl.CurlRequest.class)))
                    .thenReturn("curl");

            supportLogger.log(state);

            String message = listAppender.list.getFirst().getFormattedMessage();

            assertThat(message)
                    .contains("[BODY]\n{name=Raul}")
                    .contains("[QUERY-PARAMS]\n-")
                    .contains("[HEADERS]\n-")
                    .contains("────────────────────── RESPONSE ─────────────────────────────────────────────────\n-");
        }

        @Test
        @DisplayName("No debe utilizar serialización compacta cuando el nivel es DEBUG")
        void shouldNotUseCompactSerializationInDebugMode() throws Exception {
            SupportLogState state = mockState();
            ObjectWriter writer = mock(ObjectWriter.class);

            when(properties.resolvedLevel()).thenReturn(SupportLogLevel.DEBUG);
            when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
            when(writer.writeValueAsString(any())).thenReturn("{}");
            when(buildCurl.build(any())).thenReturn("curl");

            supportLogger.log(state);

            verify(objectMapper, never()).writeValueAsString(any());
        }
    }

    @Test
    @DisplayName("Debe consultar el nivel de logging una sola vez")
    void shouldResolveLoggingLevelOnlyOnce() throws Exception {
        SupportLogState state = mockState();

        when(properties.resolvedLevel()).thenReturn(SupportLogLevel.INFO);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        supportLogger.log(state);

        verify(properties, times(1)).resolvedLevel();
    }

    private SupportLogState mockState() {
        return SupportLogState.builder()
                .operation("retrieve-card")
                .method("POST")
                .url("https://service.test/cards")
                .queryParams(Map.of("page", "1"))
                .headers(Map.of("Authorization", "Bearer token"))
                .request(Map.of("name", "Raul"))
                .response(Map.of("status", "OK"))
                .build();
    }
}