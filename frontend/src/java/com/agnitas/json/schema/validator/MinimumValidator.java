/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class MinimumValidator extends ExtendedBaseJsonSchemaValidator {
	public MinimumValidator(final JsonObject parentValidatorData, final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(parentValidatorData, jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("Data for minimum is null", jsonSchemaPath);
		} else if (validatorData instanceof String) {
			try {
				this.validatorData = NumberUtilities.parseNumber((String) validatorData);
			} catch (final NumberFormatException e) {
				throw new JsonSchemaDefinitionError("Data for minimum '" + validatorData + "' is not a number", jsonSchemaPath, e);
			}
		} else if (!(validatorData instanceof Number)) {
			throw new JsonSchemaDefinitionError("Data for minimum '" + validatorData + "' is not a number", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		if (!(jsonNode.isNumber())) {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				throw new JsonSchemaDataValidationError("Expected data type 'number' but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
			}
		} else {
			final Number dataValue = ((Number) jsonNode.getValue()).doubleValue();
			final Number minimumValue = ((Number) validatorData).doubleValue();

			if (NumberUtilities.compare(dataValue, minimumValue) < 0) {
				throw new JsonSchemaDataValidationError("Minimum number is '" + validatorData + "' but value was '" + jsonNode.getValue()  + "'", jsonPath);
			}

			if (parentValidatorData.containsPropertyKey("exclusiveMinimum")) {
				final Object exclusiveMinimumRaw = parentValidatorData.get("exclusiveMinimum");
				if (exclusiveMinimumRaw == null) {
					throw new JsonSchemaDefinitionError("Property 'exclusiveMinimum' is 'null'", jsonSchemaPath);
				} else if (exclusiveMinimumRaw instanceof Boolean) {
					if ((Boolean) exclusiveMinimumRaw) {
						if (NumberUtilities.compare(dataValue, minimumValue) == 0) {
							throw new JsonSchemaDataValidationError("Exclusive minimum number is '" + validatorData + "' but value was '" + jsonNode.getValue()  + "'", jsonPath);
						}
					}
				} else {
					throw new JsonSchemaDefinitionError("ExclusiveMinimum data is not 'boolean'", jsonSchemaPath);
				}
			}
		}
	}
}
