package io.github.raulbolivarnavas.supportlogging.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Locale;
import java.util.Map;

/**
 * Jackson 2 implementation of {@link DataMasker} used by the Spring Boot 3 module.
 */
public class Jackson2DataMasker implements DataMasker {

    private static final char MASK_CHARACTER = '*';

    private final ObjectMapper objectMapper;
    private final MaskingProperties maskingProperties;

    /**
     * Creates a new masker.
     *
     * @param objectMapper the object mapper used to traverse payloads
     * @param maskingProperties the masking rules to apply
     */
    public Jackson2DataMasker(
            ObjectMapper objectMapper,
            MaskingProperties maskingProperties
    ) {
        this.objectMapper = objectMapper;
        this.maskingProperties = maskingProperties;
    }

    /**
     * Masks the supplied value using the configured field rules.
     *
     * @param value the value to mask
     * @return the masked value, or the original value when masking is disabled or conversion fails
     */
    @Override
    public Object mask(Object value) {
        if (value == null || !maskingProperties.resolvedEnabled()) {
            return value;
        }

        JsonNode node = objectMapper.valueToTree(value);
        maskNode(node, maskingProperties.resolvedFields());
        return node;
    }

    private void maskNode(JsonNode node, Map<String, MaskFieldProperties> fields) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;

            objectNode.properties().forEach(entry -> {
                MaskFieldProperties configuration =
                        findConfiguration(entry.getKey(), fields);

                if (configuration != null && entry.getValue().isValueNode()) {
                    objectNode.put(
                            entry.getKey(),
                            applyMask(entry.getValue().asText(), configuration)
                    );
                    return;
                }

                maskNode(entry.getValue(), fields);
            });
            return;
        }

        if (node.isArray()) {
            node.forEach(child -> maskNode(child, fields));
        }
    }

    private MaskFieldProperties findConfiguration(
            String fieldName,
            Map<String, MaskFieldProperties> fields
    ) {
        String normalized = normalize(fieldName);

        return fields.entrySet()
                .stream()
                .filter(entry -> normalize(entry.getKey()).equals(normalized))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String applyMask(String value, MaskFieldProperties configuration) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return switch (configuration.resolvedType()) {
            case FULL -> mask(value.length());
            case LEFT -> maskLeft(value, configuration.resolvedVisible());
            case RIGHT -> maskRight(value, configuration.resolvedVisible());
            case NONE -> value;
        };
    }

    private String maskLeft(String value, int visible) {
        int visibleCharacters = Math.min(visible, value.length());
        return value.substring(0, visibleCharacters)
                + mask(value.length() - visibleCharacters);
    }

    private String maskRight(String value, int visible) {
        int visibleCharacters = Math.min(visible, value.length());
        return mask(value.length() - visibleCharacters)
                + value.substring(value.length() - visibleCharacters);
    }

    private String mask(int length) {
        return String.valueOf(MASK_CHARACTER)
                .repeat(Math.max(0, length));
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim()
                        .toLowerCase(Locale.ROOT)
                        .replace("-", "")
                        .replace("_", "");
    }
}
