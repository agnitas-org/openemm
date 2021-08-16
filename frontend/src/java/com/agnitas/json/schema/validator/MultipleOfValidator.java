/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.math.BigDecimal;

import com.agnitas.json.JsonNode;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class MultipleOfValidator extends BaseJsonSchemaValidator {
	public MultipleOfValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("Data for multipleOf is null", jsonSchemaPath);
		} else if (validatorData instanceof String) {
			try {
				this.validatorData = new BigDecimal((String) validatorData);
			} catch (final NumberFormatException e) {
				throw new JsonSchemaDefinitionError("Data for multipleOf '" + validatorData + "' is not a number", jsonSchemaPath, e);
			}
		} else if (!(validatorData instanceof Number)) {
			throw new JsonSchemaDefinitionError("Data for multipleOf '" + validatorData + "' is not a number", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		if (!(jsonNode.isNumber())) {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				throw new JsonSchemaDataValidationError("Expected data type 'number' but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
			}
		} else {
			if (new BigDecimal(jsonNode.getValue().toString()).remainder(new BigDecimal(validatorData.toString())).compareTo(BigDecimal.ZERO) != 0) {
				throw new JsonSchemaDataValidationError("Number must be multiple of '" + ((Number) validatorData).toString() + "' but value was '" + jsonNode.getValue()  + "'", jsonPath);
			}
		}
	}
}
