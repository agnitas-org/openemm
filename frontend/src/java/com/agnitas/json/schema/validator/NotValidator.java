/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.util.List;

import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.schema.JsonSchema;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class NotValidator extends BaseJsonSchemaValidator {
	private List<BaseJsonSchemaValidator> subValidators = null;

	public NotValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("Not-validation data is 'null'", jsonSchemaPath);
		} else if (validatorData instanceof JsonObject) {
			subValidators = JsonSchema.createValidators((JsonObject) validatorData, jsonSchemaDependencyResolver, jsonSchemaPath, jsonNode, jsonPath);
			if (subValidators == null || subValidators.size() == 0) {
				throw new JsonSchemaDefinitionError("Not-validation JsonObject is empty", jsonSchemaPath);
			}
		} else {
			throw new JsonSchemaDefinitionError("Not-validation property does not have an JsonObject value", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		boolean didNotApply = false;
		try {
			for (final BaseJsonSchemaValidator subValidator : subValidators) {
				subValidator.validate();
			}
		} catch (final JsonSchemaDataValidationError e) {
			didNotApply = true;
		}
		if (!didNotApply) {
			throw new JsonSchemaDataValidationError("The 'not' property did apply to JsonNode", jsonPath);
		}
	}
}
