/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.schema.JsonSchema;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class AnyOfValidator extends BaseJsonSchemaValidator {
	private List<List<BaseJsonSchemaValidator>> subValidatorPackages = null;

	public AnyOfValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("AnyOf array is 'null'", jsonSchemaPath);
		} else if (validatorData instanceof JsonArray) {
			subValidatorPackages = new ArrayList<>();
			for (int i = 0; i < ((JsonArray) validatorData).size(); i++) {
				final Object subValidationData = ((JsonArray) validatorData).get(i);
				if (subValidationData instanceof JsonObject) {
					subValidatorPackages.add(JsonSchema.createValidators((JsonObject) subValidationData, jsonSchemaDependencyResolver, jsonSchemaPath, jsonNode, jsonPath));
				} else {
					throw new JsonSchemaDefinitionError("AnyOf array contains a non-JsonObject", jsonSchemaPath);
				}
			}
			if (subValidatorPackages == null || subValidatorPackages.size() == 0) {
				throw new JsonSchemaDefinitionError("AnyOf array is empty", jsonSchemaPath);
			}
		} else {
			throw new JsonSchemaDefinitionError("AnyOf property does not have an array value", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		for (final List<BaseJsonSchemaValidator> subValidatorPackage : subValidatorPackages) {
			try {
				for (final BaseJsonSchemaValidator subValidator : subValidatorPackage) {
					subValidator.validate();
				}
				return;
			} catch (final JsonSchemaDataValidationError e) {
				// Do nothing, only one subvalidator must have successfully validated
			}
		}
		throw new JsonSchemaDataValidationError("No option of 'anyOf' property did apply to JsonNode", jsonPath);
	}
}
