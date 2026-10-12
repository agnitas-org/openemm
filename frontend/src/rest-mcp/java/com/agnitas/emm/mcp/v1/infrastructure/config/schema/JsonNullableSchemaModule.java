/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.config.schema;

import com.fasterxml.classmate.ResolvedType;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.impl.module.FlattenedWrapperModule;
import org.openapitools.jackson.nullable.JsonNullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Unwraps {@link JsonNullable JsonNullable&lt;T&gt;} to {@code T} and adds {@code null} to its JSON Schema type.
 * Remove once Spring AI provides native {@code JsonNullable} schema support
 * (<a href="https://github.com/spring-projects/spring-ai/issues/6046">issue #6046</a> /
 * <a href="https://github.com/spring-projects/spring-ai/pull/6071">PR #6071</a>).
 */
public final class JsonNullableSchemaModule extends FlattenedWrapperModule<JsonNullable> {

    public JsonNullableSchemaModule() {
        super(JsonNullable.class);
    }

    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        builder.forTypesInGeneral().withCustomDefinitionProvider((javaType, context) -> {
            if (!this.isWrapperType(javaType)) {
                return null;
            }
            ResolvedType wrappedType = context.getTypeContext().getTypeParameterFor(javaType, JsonNullable.class, 0);
            if (wrappedType == null) {
                return null;
            }
            ObjectNode schema = context.createStandardDefinition(wrappedType, null);
            addNullToType(schema);
            addNullToEnum(schema);
            return new CustomDefinition(schema, CustomDefinition.DefinitionType.INLINE,
                    CustomDefinition.AttributeInclusion.YES);
        });
    }

    private static void addNullToType(ObjectNode schema) {
        JsonNode typeNode = schema.get("type");
        if (typeNode == null) {
            ArrayNode types = schema.putArray("type");
            types.add("null");
            return;
        }
        if (typeNode.isTextual()) {
            String existing = typeNode.asText();
            ArrayNode types = schema.putArray("type");
            types.add(existing);
            types.add("null");
            return;
        }
        if (typeNode.isArray()) {
            ArrayNode types = (ArrayNode) typeNode;
            for (int i = 0; i < types.size(); i++) {
                if ("null".equals(types.get(i).asText())) {
                    return;
                }
            }
            types.add("null");
        }
    }

    // MCP schema validation requires null to be present in the enum values.
    private static void addNullToEnum(ObjectNode schema) {
        JsonNode enumNode = schema.get("enum");
        if (enumNode == null || !enumNode.isArray()) {
            return;
        }
        ArrayNode enumValues = (ArrayNode) enumNode;
        for (JsonNode value : enumValues) {
            if (value.isNull()) {
                return;
            }
        }
        enumValues.addNull();
    }

}
