package io.github.raulbolivarnavas.supportlogging.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class Jackson2DataMaskerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void maskShouldApplyConfiguredRulesRecursively() {
        Map<String, MaskFieldProperties> fields = new LinkedHashMap<>();
        fields.put("secret", new MaskFieldProperties(MaskType.FULL, null));
        fields.put("left-field", new MaskFieldProperties(MaskType.LEFT, 2));
        fields.put("rightField", new MaskFieldProperties(MaskType.RIGHT, 3));
        fields.put("none_field", new MaskFieldProperties(MaskType.NONE, null));
        fields.put("api-key", new MaskFieldProperties(MaskType.FULL, null));

        MaskingProperties maskingProperties = new MaskingProperties(true, fields);
        Jackson2DataMasker masker = new Jackson2DataMasker(objectMapper, maskingProperties);

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
        Jackson2DataMasker masker = new Jackson2DataMasker(objectMapper, maskingProperties);
        List<String> payload = new ArrayList<>(List.of("one", "two"));

        assertSame(payload, masker.mask(payload));
        assertNull(masker.mask(null));
    }
}
