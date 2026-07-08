/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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

public class AllOfValidator extends BaseJsonSchemaValidator {

	private final List<List<BaseJsonSchemaValidator>> subValidatorPackages;

	public AllOfValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

        if (validatorData instanceof JsonArray validatorDataArray) {
            subValidatorPackages = new ArrayList<>();
            for (int i = 0; i < validatorDataArray.size(); i++) {
                if (validatorDataArray.get(i) instanceof JsonObject subValidationData) {
                    subValidatorPackages.add(JsonSchema.createValidators(subValidationData, jsonSchemaDependencyResolver, jsonSchemaPath, jsonNode, jsonPath));
                } else {
                    throw new JsonSchemaDefinitionError("AllOf array contains a non-JsonObject", jsonSchemaPath);
                }
            }
            if (subValidatorPackages.isEmpty()) {
                throw new JsonSchemaDefinitionError("AllOf array is empty", jsonSchemaPath);
            }
        } else {
            throw new JsonSchemaDefinitionError("AllOf property does not have an array value", jsonSchemaPath);
        }
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		for (final List<BaseJsonSchemaValidator> subValidatorPackage : subValidatorPackages) {
			try {
				for (final BaseJsonSchemaValidator subValidator : subValidatorPackage) {
					subValidator.validate();
				}
			} catch (final JsonSchemaDataValidationError e) {
				throw new JsonSchemaDataValidationError("Some option of 'allOf' property did not apply to JsonNode", jsonPath, e);
			}
		}
	}
}
