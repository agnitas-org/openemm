/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class MinItemsValidator extends BaseJsonSchemaValidator {
	public MinItemsValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("Data for minimum items is 'null'", jsonSchemaPath);
		} else if (validatorData instanceof Integer) {
			if (((Integer) validatorData) < 0) {
				throw new JsonSchemaDefinitionError("Data for minimum items amount is  negative", jsonSchemaPath);
			}
		} else if (validatorData instanceof String) {
			try {
				this.validatorData = Integer.parseInt((String) validatorData);
			} catch (final NumberFormatException e) {
				throw new JsonSchemaDefinitionError("Data for minimum items '" + validatorData + "' is not a number", jsonSchemaPath, e);
			}
		}
	}

	@Override
	public void validate() throws JsonSchemaDataValidationError {
		if (!(jsonNode.isJsonArray())) {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				throw new JsonSchemaDataValidationError("Expected data type 'array' but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
			}
		} else {
			if (((JsonArray) jsonNode.getValue()).size() < ((Integer) validatorData)) {
				throw new JsonSchemaDataValidationError("Required minimum number of items is '" + validatorData + "' but was '" + ((JsonArray) jsonNode.getValue()).size()  + "'", jsonPath);
			}
		}
	}
}
