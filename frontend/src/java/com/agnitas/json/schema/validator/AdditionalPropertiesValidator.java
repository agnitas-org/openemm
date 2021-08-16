/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.schema.JsonSchema;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class AdditionalPropertiesValidator extends ExtendedBaseJsonSchemaValidator {
	public AdditionalPropertiesValidator(final JsonObject parentValidatorData, final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(parentValidatorData, jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("AdditionalProperties data is 'null'", jsonSchemaPath);
		} else if (!(validatorData instanceof Boolean) && !(validatorData instanceof JsonObject)) {
			throw new JsonSchemaDefinitionError("AdditionalProperties data is not a 'boolean' or 'object'", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		if (!(jsonNode.isJsonObject())) {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				throw new JsonSchemaDataValidationError("Expected data type 'object' but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
			}
		} else {
			final List<String> additionalPropertyNames = new ArrayList<>();

			for (final String checkPropertyKey : ((JsonObject) jsonNode.getValue()).keySet()) {
				if (parentValidatorData.containsPropertyKey("properties")) {
					if (parentValidatorData.get("properties") == null) {
						throw new JsonSchemaDefinitionError("Properties data is 'null'", jsonSchemaPath);
					} else if (!(parentValidatorData.get("properties") instanceof JsonObject)) {
						throw new JsonSchemaDefinitionError("Properties data is not a JsonObject", jsonSchemaPath);
					} else if (((JsonObject) (parentValidatorData).get("properties")).containsPropertyKey(checkPropertyKey)) {
						continue;
					}
				}

				boolean isAdditionalPropertyKey = true;

				if (parentValidatorData.containsPropertyKey("patternProperties")) {
					if (parentValidatorData.get("patternProperties") == null) {
						throw new JsonSchemaDefinitionError("PatternProperties data is 'null'", jsonSchemaPath);
					} else if (!(parentValidatorData.get("patternProperties") instanceof JsonObject)) {
						throw new JsonSchemaDefinitionError("PatternProperties data is not a JsonObject", jsonSchemaPath);
					} else {
						for (final Entry<String, Object> entry : ((JsonObject) parentValidatorData.get("patternProperties")).entrySet()) {
							if (entry.getValue() == null || !(entry.getValue() instanceof JsonObject)) {
								throw new JsonSchemaDefinitionError("PatternProperties data contains a non-JsonObject", jsonSchemaPath);
							} else {
								Pattern propertyKeyPattern;
								try {
									propertyKeyPattern = Pattern.compile(entry.getKey());
								} catch (final Exception e1) {
									throw new JsonSchemaDefinitionError("PatternProperties data contains invalid RegEx pattern: " + entry.getKey(), jsonSchemaPath, e1);
								}

								if (propertyKeyPattern.matcher(checkPropertyKey).find()) {
									isAdditionalPropertyKey = false;
									break;
								}
							}
						}
					}
				}

				if (isAdditionalPropertyKey) {
					additionalPropertyNames.add(checkPropertyKey);
				}
			}

			if (additionalPropertyNames.size() > 0) {
				if (validatorData instanceof Boolean) {
					if (!(Boolean) validatorData) {
						throw new JsonSchemaDataValidationError("Unexpected property keys found '" + StringUtils.join(additionalPropertyNames, "', '") + "'", jsonPath);
					}
				} else if (validatorData instanceof JsonObject) {
					for (final String propertyKey : additionalPropertyNames) {
						JsonNode newJsonNode;
						try {
							newJsonNode = new JsonNode(((JsonObject) jsonNode.getValue()).get(propertyKey));
						} catch (final Exception e) {
							throw new JsonSchemaDataValidationError("Invalid data type '" + ((JsonObject) jsonNode.getValue()).get(propertyKey).getClass().getSimpleName() + "'", jsonPath + "." + propertyKey, e);
						}
						final List<BaseJsonSchemaValidator> subValidators = JsonSchema.createValidators(((JsonObject) validatorData), jsonSchemaDependencyResolver, jsonSchemaPath, newJsonNode, jsonPath + "." + propertyKey);
						for (final BaseJsonSchemaValidator subValidator : subValidators) {
							subValidator.validate();
						}
					}
				}
			}
		}
	}
}
