/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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

public class OneOfValidator extends BaseJsonSchemaValidator {
	private List<List<BaseJsonSchemaValidator>> subValidatorPackages = null;

	public OneOfValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("OneOf array is 'null'", jsonSchemaPath);
		} else if (validatorData instanceof JsonArray) {
			subValidatorPackages = new ArrayList<>();
			for (final Object subValidationData : ((JsonArray) validatorData)) {
				if (subValidationData instanceof JsonObject) {
					subValidatorPackages.add(JsonSchema.createValidators((JsonObject) subValidationData, jsonSchemaDependencyResolver, jsonSchemaPath, jsonNode, jsonPath));
				} else {
					throw new JsonSchemaDefinitionError("OneOf array contains a non-JsonObject", jsonSchemaPath);
				}
			}
			if (subValidatorPackages == null || subValidatorPackages.size() == 0) {
				throw new JsonSchemaDefinitionError("OneOf array is empty", jsonSchemaPath);
			}
		} else {
			throw new JsonSchemaDefinitionError("OneOf property does not have an array value", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		int applyCount = 0;
		for (final List<BaseJsonSchemaValidator> subValidatorPackage : subValidatorPackages) {
			try {
				for (final BaseJsonSchemaValidator subValidator : subValidatorPackage) {
					subValidator.validate();
				}
				applyCount++;
			} catch (@SuppressWarnings("unused") final JsonSchemaDataValidationError e) {
				// Do nothing, exactly one subvalidator must have successfully validated
			}
		}

		if (applyCount < 1) {
			throw new JsonSchemaDataValidationError("No option of 'oneOf' property did apply to JsonNode", jsonPath);
		} else if (applyCount > 1) {
			throw new JsonSchemaDataValidationError("More than one option of 'oneOf' property did apply to JsonNode", jsonPath);
		}
	}
}
