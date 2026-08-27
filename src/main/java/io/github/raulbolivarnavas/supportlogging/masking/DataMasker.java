package io.github.raulbolivarnavas.supportlogging.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.raulbolivarnavas.supportlogging.config.SupportLoggingProperties;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class DataMasker {

    private static final char MASK_CHARACTER = '*';

    private final ObjectMapper objectMapper;
    private final SupportLoggingProperties properties;

    public Object mask(Object value) {

        if (value == null) {
            return null;
        }

        MaskingProperties masking =
                properties.resolvedMasking();

        if (!masking.resolvedEnabled()) {
            return value;
        }

        JsonNode node =
                objectMapper.valueToTree(value);

        maskNode(
                node,
                masking.resolvedFields()
        );

        return node;
    }

    private void maskNode(
            JsonNode node,
            Map<String, MaskFieldProperties> fields
    ) {

        if (node == null) {
            return;
        }

        if (node.isObject()) {

            ObjectNode objectNode =
                    (ObjectNode) node;

            objectNode.fields()
                    .forEachRemaining(entry -> {

                        MaskFieldProperties configuration =
                                findConfiguration(
                                        entry.getKey(),
                                        fields
                                );

                        if (configuration != null
                                && entry.getValue().isValueNode()) {

                            String value =
                                    entry.getValue().asText();

                            objectNode.put(
                                    entry.getKey(),
                                    applyMask(
                                            value,
                                            configuration
                                    )
                            );

                            return;
                        }

                        maskNode(
                                entry.getValue(),
                                fields
                        );
                    });

            return;
        }

        if (node.isArray()) {
            node.forEach(child ->
                    maskNode(child, fields)
            );
        }
    }

    private MaskFieldProperties findConfiguration(
            String fieldName,
            Map<String, MaskFieldProperties> fields
    ) {

        String normalized =
                normalize(fieldName);

        return fields.entrySet()
                .stream()
                .filter(entry ->
                        normalize(entry.getKey())
                                .equals(normalized)
                )
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String applyMask(
            String value,
            MaskFieldProperties configuration
    ) {

        if (value == null || value.isEmpty()) {
            return value;
        }

        return switch (configuration.resolvedType()) {

            case FULL ->
                    mask(value.length());

            case KEEP_LEFT ->
                    maskLeft(
                            value,
                            configuration.resolvedVisible()
                    );

            case KEEP_RIGHT ->
                    maskRight(
                            value,
                            configuration.resolvedVisible()
                    );

            case NONE ->
                    value;
        };
    }

    private String maskLeft(
            String value,
            int visible
    ) {

        int visibleCharacters =
                Math.min(visible, value.length());

        return value.substring(
                0,
                visibleCharacters
        )
                + mask(
                value.length()
                        - visibleCharacters
        );
    }

    private String maskRight(
            String value,
            int visible
    ) {

        int visibleCharacters =
                Math.min(visible, value.length());

        return mask(
                value.length()
                        - visibleCharacters
        )
                + value.substring(
                value.length()
                        - visibleCharacters
        );
    }

    private String mask(int length) {
        return String.valueOf(MASK_CHARACTER)
                .repeat(Math.max(0, length));
    }

    private String normalize(String value) {

        return value == null
                ? ""
                : value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "");
    }
}