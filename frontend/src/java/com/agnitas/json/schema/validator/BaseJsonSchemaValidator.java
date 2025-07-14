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

public abstract class BaseJsonSchemaValidator {
	protected JsonSchemaDependencyResolver jsonSchemaDependencyResolver;
	protected String jsonSchemaPath;
	protected Object validatorData;
	protected JsonNode jsonNode;
	protected String jsonPath;
	
	protected BaseJsonSchemaValidator(JsonSchemaDependencyResolver jsonSchemaDependencyResolver, String jsonSchemaPath, Object validatorData, JsonNode jsonNode, String jsonPath) throws JsonSchemaDefinitionError {
		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("ValidatorData is 'null'", jsonSchemaPath);
		}
		
		this.jsonSchemaDependencyResolver = jsonSchemaDependencyResolver;
		this.jsonSchemaPath = jsonSchemaPath;
		this.validatorData = validatorData;
		this.jsonNode = jsonNode;
		this.jsonPath = jsonPath;
	}

	public abstract void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError;
}
