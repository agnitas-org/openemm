/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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

public class ReferenceValidator extends BaseJsonSchemaValidator {
	private JsonObject dereferencedSchemaObject;

	public ReferenceValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		try {
			if (validatorData == null) {
				throw new JsonSchemaDefinitionError("Reference key is 'null'", jsonSchemaPath);
			} else if (!(validatorData instanceof String)) {
				throw new JsonSchemaDefinitionError("Reference key is not a 'string'", jsonSchemaPath);
			} else if (jsonSchemaDependencyResolver == null) {
				throw new JsonSchemaDefinitionError("JSON schema reference definitions is empty. Cannot dereference key '" + validatorData + "'", jsonSchemaPath);
			} else {
				jsonSchemaDependencyResolver.checkCyclicDependency(jsonPath, (String) validatorData, jsonSchemaPath);

				final Object dereferencedValue = jsonSchemaDependencyResolver.getDependencyByReference((String) validatorData, jsonSchemaPath);
				if (dereferencedValue == null) {
					throw new JsonSchemaDefinitionError("Invalid JSON schema reference data type for key '" + validatorData + "'. Expected 'object' but was 'null'", jsonSchemaPath);
				} else if (!(dereferencedValue instanceof JsonObject)) {
					throw new JsonSchemaDefinitionError("Invalid JSON schema reference data type for key '" + validatorData + "'. Expected 'object' but was '" + dereferencedValue.getClass().getSimpleName() + "'", jsonSchemaPath);
				} else {
					dereferencedSchemaObject = (JsonObject) dereferencedValue;
					this.jsonSchemaPath = ((String) validatorData);
				}
			}
		} catch (final JsonSchemaDefinitionError e) {
			throw e;
		} catch (final Exception e) {
			throw new JsonSchemaDefinitionError("Error '" + e.getClass().getSimpleName() + "' while resolving JSON schema reference '" + validatorData + "': " + e.getMessage(), jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		if (dereferencedSchemaObject == null) {
			throw new JsonSchemaDefinitionError("Invalid JSON schema reference data type for key '" + validatorData + "'. Expected 'object' but was 'null'", jsonSchemaPath);
		}

		final List<BaseJsonSchemaValidator> subValidators = JsonSchema.createValidators(dereferencedSchemaObject, jsonSchemaDependencyResolver, jsonSchemaPath, jsonNode, jsonPath);
		for (final BaseJsonSchemaValidator subValidator : subValidators) {
			subValidator.validate();
		}
	}
}
