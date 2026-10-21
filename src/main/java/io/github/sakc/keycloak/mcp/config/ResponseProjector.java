package io.github.sakc.keycloak.mcp.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sakc.keycloak.mcp.domain.service.ResponseFieldCatalog;

/**
 * Trims read-tool payloads to a minimal field set. Callers may pass {@code fields} (array or comma separated string)
 * to request specific attributes, or {@code ["*"]} to receive the full representation.
 */
final class ResponseProjector {

    static final String FIELDS_ARGUMENT = "fields";

    private final ObjectMapper objectMapper;

    private final ResponseFieldCatalog catalog;

    ResponseProjector(ObjectMapper objectMapper, ResponseFieldCatalog catalog) {
        this.objectMapper = objectMapper;
        this.catalog = catalog;
    }

    boolean supports(String tool) {
        return catalog.supports(tool);
    }

    /** Returns the requested fields, the tool default, or null when the full payload was requested. */
    List<String> resolveFields(String tool, JsonNode arguments) {
        JsonNode requested = arguments == null ? null : arguments.get(FIELDS_ARGUMENT);
        if (requested == null || requested.isNull()) {
            return catalog.defaultFields(tool);
        }
        List<String> fields = new ArrayList<>();
        if (requested.isArray()) {
            requested.forEach(item -> addField(fields, item.asText()));
        }
        else {
            for (String part : requested.asText().split(",")) {
                addField(fields, part);
            }
        }
        if (fields.isEmpty()) {
            return catalog.defaultFields(tool);
        }
        return fields.contains("*") || fields.contains("all") ? null : fields;
    }

    JsonNode project(Object data, List<String> fields) {
        JsonNode node = data instanceof JsonNode json ? json : objectMapper.valueToTree(data);
        if (fields == null) {
            return node;
        }
        if (node.isArray()) {
            ArrayNode result = objectMapper.createArrayNode();
            node.forEach(item -> result.add(projectObject(item, fields)));
            return result;
        }
        return projectObject(node, fields);
    }

    private JsonNode projectObject(JsonNode node, List<String> fields) {
        if (!node.isObject()) {
            return node;
        }
        ObjectNode result = objectMapper.createObjectNode();
        for (String field : fields) {
            JsonNode value = lookup(node, field);
            if (value != null && !value.isMissingNode()) {
                result.set(field, value);
            }
        }
        return result;
    }

    // Dotted paths such as attributes.foo address nested values.
    private JsonNode lookup(JsonNode node, String path) {
        JsonNode current = node;
        for (String segment : path.split("\\.")) {
            if (current == null || !current.isObject()) {
                return null;
            }
            current = current.get(segment);
        }
        return current;
    }

    private void addField(List<String> fields, String value) {
        String trimmed = value == null ? "" : value.trim();
        if (!trimmed.isEmpty()) {
            fields.add(trimmed);
        }
    }
}
