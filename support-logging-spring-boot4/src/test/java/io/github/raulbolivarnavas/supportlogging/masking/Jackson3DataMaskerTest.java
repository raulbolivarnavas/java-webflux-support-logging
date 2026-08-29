package io.github.raulbolivarnavas.supportlogging.masking;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class Jackson3DataMaskerTest {

    @Test
    void maskShouldApplyConfiguredRulesRecursively() {
        Map<String, MaskFieldProperties> fields = new LinkedHashMap<>();
        fields.put("secret", new MaskFieldProperties(MaskType.FULL, null));
        fields.put("left-field", new MaskFieldProperties(MaskType.LEFT, 2));
        fields.put("rightField", new MaskFieldProperties(MaskType.RIGHT, 3));
        fields.put("none_field", new MaskFieldProperties(MaskType.NONE, null));
        fields.put("api-key", new MaskFieldProperties(MaskType.FULL, null));

        MaskingProperties maskingProperties = new MaskingProperties(true, fields);
        Jackson3DataMasker masker =
                new Jackson3DataMasker(JsonMapper.builder().build(), maskingProperties);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("secret", "abcdef");
        payload.put("leftField", "abcdef");
        payload.put("right_field", "abcdef");
        payload.put("noneField", "abcdef");
        payload.put(
                "nested",
                List.of(new LinkedHashMap<>(Map.of("apiKey", "12345")))
        );

        JsonNode masked = (JsonNode) masker.mask(payload);

        assertEquals(
                "{\"secret\":\"******\",\"leftField\":\"ab****\",\"right_field\":\"***def\","
                        + "\"noneField\":\"abcdef\",\"nested\":[{\"apiKey\":\"*****\"}]}",
                masked.toString()
        );
    }

    @Test
    void maskShouldReturnOriginalValueWhenDisabledOrNull() {
        MaskingProperties maskingProperties = new MaskingProperties(false, Map.of());
        Jackson3DataMasker masker =
                new Jackson3DataMasker(JsonMapper.builder().build(), maskingProperties);
        List<String> payload = new ArrayList<>(List.of("one", "two"));

        assertSame(payload, masker.mask(payload));
        assertNull(masker.mask(null));
    }

    @Test
    void maskShouldReturnOriginalValueWhenConversionFails() {
        MaskingProperties maskingProperties =
                new MaskingProperties(true, Map.of("secret", new MaskFieldProperties(MaskType.FULL, null)));
        Jackson3DataMasker masker =
                new Jackson3DataMasker(JsonMapper.builder().build(), maskingProperties);
        ExplodingBean payload = new ExplodingBean();

        assertSame(payload, masker.mask(payload));
    }

    static final class ExplodingBean {
        public String getSecret() {
            throw new IllegalStateException("boom");
        }
    }
}
