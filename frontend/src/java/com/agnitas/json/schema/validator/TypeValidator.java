/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.util.List;

import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonDataType;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.schema.JsonSchema;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class TypeValidator extends BaseJsonSchemaValidator {
	public TypeValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("Type data is 'null'", jsonSchemaPath);
		} else if (!(validatorData instanceof String) && !(validatorData instanceof JsonArray)) {
			throw new JsonSchemaDefinitionError("Type data is not a 'string' or 'array'", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		if (validatorData instanceof JsonArray) {
			for (final Object typeData : ((JsonArray) validatorData)) {
				if (typeData == null) {
					throw new JsonSchemaDefinitionError("Type data array contains a 'null' item", jsonSchemaPath);
				} else if (typeData instanceof String) {
					if ("any".equals(typeData)) {
						return;
					} else {
						JsonDataType jsonDataType;
						try {
							jsonDataType = JsonDataType.getFromString((String) typeData);
						} catch (final Exception e) {
							throw new JsonSchemaDefinitionError("Invalid JSON data type '" + validatorData + "'", jsonSchemaPath, e);
						}

						if (jsonNode.getJsonDataType() == jsonDataType || (jsonDataType == JsonDataType.NUMBER && jsonNode.getJsonDataType() == JsonDataType.INTEGER)) {
							return;
						}
					}
				} else if (typeData instanceof JsonObject) {
					final List<BaseJsonSchemaValidator> subValidators = JsonSchema.createValidators((JsonObject) typeData, jsonSchemaDependencyResolver, jsonSchemaPath, jsonNode, jsonPath);
					try {
						for (final BaseJsonSchemaValidator subValidator : subValidators) {
							subValidator.validate();
						}
						return;
					} catch (final JsonSchemaDataValidationError e) {
						// Do nothing, just check the next array item
					}
				} else {
					throw new JsonSchemaDefinitionError("Type data array contains a item that is no 'string' and no 'object'", jsonSchemaPath);
				}
			}
			throw new JsonSchemaDataValidationError("Invalid data type '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
		} else {
			if ("any".equals(validatorData)) {
				return;
			} else {
				JsonDataType jsonDataType;
				try {
					jsonDataType = JsonDataType.getFromString((String) validatorData);
				} catch (final Exception e) {
					throw new JsonSchemaDefinitionError("Invalid JSON data type '" + validatorData + "'", jsonSchemaPath, e);
				}

				if (jsonNode.getJsonDataType() != jsonDataType && !(jsonDataType == JsonDataType.NUMBER && jsonNode.getJsonDataType() == JsonDataType.INTEGER)) {
					throw new JsonSchemaDataValidationError("Expected data type is '" + (String) validatorData + "' but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
				}
			}
		}
	}
}
