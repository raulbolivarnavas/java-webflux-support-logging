package io.github.raulbolivarnavas.supportlogging.json;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Jackson3JsonFormatterTest {

    private final Jackson3JsonFormatter formatter =
            new Jackson3JsonFormatter(JsonMapper.builder().build());

    @Test
    void compactShouldHandleNullStringObjectsAndFailures() {
        ExplodingBean bean = new ExplodingBean();

        assertEquals("null", formatter.compact(null));
        assertEquals("text", formatter.compact("text"));
        assertEquals("{\"value\":\"ok\"}", formatter.compact(new SimpleBean("ok")));
        assertEquals("exploding", formatter.compact(bean));
    }

    @Test
    void prettyShouldHandleNullStringObjectsAndFailures() {
        ExplodingBean bean = new ExplodingBean();

        assertEquals("null", formatter.pretty(null));
        assertEquals("text", formatter.pretty("text"));
        assertTrue(formatter.pretty(new SimpleBean("ok")).contains("\"value\" : \"ok\""));
        assertEquals("exploding", formatter.pretty(bean));
    }

    static final class SimpleBean {
        private final String value;

        private SimpleBean(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    static final class ExplodingBean {
        public String getValue() {
            throw new IllegalStateException("boom");
        }

        @Override
        public String toString() {
            return "exploding";
        }
    }
}
