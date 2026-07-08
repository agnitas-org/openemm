/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.schema.JsonSchema;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class PatternPropertiesValidator extends BaseJsonSchemaValidator {
	public PatternPropertiesValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("PatternProperties data is 'null'", jsonSchemaPath);
		} else if (!(validatorData instanceof JsonObject)) {
			throw new JsonSchemaDefinitionError("PatternProperties data is not a JsonObject", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		if (!(jsonNode.isJsonObject())) {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				throw new JsonSchemaDataValidationError("Expected data type 'object' but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
			}
		} else {
			for (final Entry<String, Object> propertyEntry : ((JsonObject) jsonNode.getValue()).entrySet()) {
				for (final Entry<String, Object> entry : ((JsonObject) validatorData).entrySet()) {
					if (entry.getValue() == null || !(entry.getValue() instanceof JsonObject)) {
						throw new JsonSchemaDefinitionError("PatternProperties data contains a non-JsonObject", jsonSchemaPath);
					} else {
						Pattern propertyKeyPattern;
						try {
							propertyKeyPattern = Pattern.compile(entry.getKey());
						} catch (final Exception e) {
							throw new JsonSchemaDefinitionError("PatternProperties data contains invalid RegEx pattern: " + entry.getKey(), jsonSchemaPath, e);
						}

						if (propertyKeyPattern.matcher(propertyEntry.getKey()).find()) {
							JsonNode nextJsonNode;
							try {
								nextJsonNode = new JsonNode(propertyEntry.getValue());
							} catch (final Exception e) {
								throw new JsonSchemaDataValidationError("Invalid property data type was '" + propertyEntry.getValue().getClass().getSimpleName() + "'", jsonPath + "." + propertyEntry.getKey(), e);
							}
							final List<BaseJsonSchemaValidator> subValidators = JsonSchema.createValidators(((JsonObject) entry.getValue()), jsonSchemaDependencyResolver, jsonSchemaPath + "." + propertyEntry.getKey(), nextJsonNode, jsonPath + "." + propertyEntry.getKey());
							for (final BaseJsonSchemaValidator subValidator : subValidators) {
								subValidator.validate();
							}
						}
					}
				}
			}
		}
	}
}
