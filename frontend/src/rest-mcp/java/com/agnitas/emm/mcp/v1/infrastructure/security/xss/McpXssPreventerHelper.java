/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.security.xss;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.http.RequestParameterXssPreventerHelper;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class McpXssPreventerHelper extends RequestParameterXssPreventerHelper {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    public McpXssPreventerHelper() {
        super(s -> false);
    }

    public Map<String, Set<HtmlCheckError>> validate(
            Map<String, Object> arguments,
            Set<String> jsonFields,
            Predicate<String> excluded
    ) {
        Map<String, Set<HtmlCheckError>> errors = new LinkedHashMap<>();
        arguments.forEach((name, value) -> check(value, name, jsonFields, excluded, errors));
        return errors;
    }

    private void check(
            Object value,
            String path,
            Set<String> jsonFields,
            Predicate<String> excluded,
            Map<String, Set<HtmlCheckError>> errors
    ) {
        switch (value) {
            case String text -> checkText(text, path, jsonFields, excluded, errors);
            case JsonNullable<?> nullable -> {
                if (nullable.isPresent()) {
                    check(nullable.get(), path, jsonFields, excluded, errors);
                }
            }
            case Map<?, ?> map -> map.forEach((key, v) ->
                    check(v, join(path, String.valueOf(key)), jsonFields, excluded, errors));
            case Iterable<?> iterable -> {
                int index = 0;
                for (Object item : iterable) {
                    check(item, path + "[" + index++ + "]", jsonFields, excluded, errors);
                }
            }
            case Record rec -> {
                for (RecordComponent component : rec.getClass().getRecordComponents()) {
                    check(read(rec, component), join(path, component.getName()), jsonFields, excluded, errors);
                }
            }
            case null, default -> {
                // null, numbers, booleans, enums and framework objects can't contain HTML
            }
        }
    }

    private void checkText(
            String text,
            String path,
            Set<String> jsonFields,
            Predicate<String> excluded,
            Map<String, Set<HtmlCheckError>> errors
    ) {
        if (excluded.test(path)) {
            return;
        }
        if (jsonFields.contains(path)) {
            try {
                checkJson(JSON_MAPPER.readTree(text), path, excluded, errors);
                return;
            } catch (JacksonException e) {
                // not a valid JSON, so check it as a plain text
            }
        }
        collect(errors, path, text);
    }

    private void checkJson(JsonNode node, String path, Predicate<String> excluded, Map<String, Set<HtmlCheckError>> errors) {
        if (node.isValueNode()) {
            if (!excluded.test(path)) {
                collect(errors, path, node.asString(""));
            }
        } else if (node.isObject()) {
            node.properties().forEach(field ->
                    checkJson(field.getValue(), join(path, field.getKey()), excluded, errors));
        } else if (node.isArray()) {
            int index = 0;
            for (JsonNode child : node) {
                checkJson(child, path + "[" + index++ + "]", excluded, errors);
            }
        }
    }

    private void collect(Map<String, Set<HtmlCheckError>> errors, String path, String text) {
        Set<HtmlCheckError> found = new HashSet<>(getHtmlCheckErrors(text));
        if (!found.isEmpty()) {
            errors.merge(path, found, (a, b) -> {
                a.addAll(b);
                return a;
            });
        }
    }

    private static Object read(Record rec, RecordComponent component) {
        try {
            Method accessor = component.getAccessor();
            accessor.setAccessible(true);
            return accessor.invoke(rec);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't read " + component.getName() + " of " + rec.getClass().getName(), e);
        }
    }

    private static String join(String path, String name) {
        return path.isEmpty() ? name : path + "." + name;
    }

}
