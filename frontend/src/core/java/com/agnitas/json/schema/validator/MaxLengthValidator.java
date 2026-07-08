/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import com.agnitas.json.JsonNode;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class MaxLengthValidator extends BaseJsonSchemaValidator {
	public MaxLengthValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (!(validatorData instanceof Integer)) {
			throw new JsonSchemaDefinitionError("Data for maxLength is not an integer", jsonSchemaPath);
		} else if (validatorData instanceof String) {
			try {
				this.validatorData = Integer.parseInt((String) validatorData);
			} catch (final NumberFormatException e) {
				throw new JsonSchemaDefinitionError("Data for maxLength '" + validatorData + "' is not an integer", jsonSchemaPath, e);
			}
		} else if (((Integer) validatorData) < 0) {
			throw new JsonSchemaDefinitionError("Data for maxLength is negative", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDataValidationError {
		if (!(jsonNode.isString())) {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				throw new JsonSchemaDataValidationError("Expected data type 'string' but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
			}
		} else {
			if (((String) jsonNode.getValue()).length() > ((Integer) validatorData)) {
				throw new JsonSchemaDataValidationError("String maxLength is '" + validatorData + "' but was '" + ((String) jsonNode.getValue()).length()  + "'", jsonPath);
			}
		}
	}
}
