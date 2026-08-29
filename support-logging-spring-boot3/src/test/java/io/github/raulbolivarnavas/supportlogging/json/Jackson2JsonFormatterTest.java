package io.github.raulbolivarnavas.supportlogging.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Jackson2JsonFormatterTest {

    private final Jackson2JsonFormatter formatter =
            new Jackson2JsonFormatter(new ObjectMapper());

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
