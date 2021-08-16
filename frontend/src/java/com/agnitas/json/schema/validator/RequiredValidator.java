/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class RequiredValidator extends BaseJsonSchemaValidator {
	public RequiredValidator(JsonSchemaDependencyResolver jsonSchemaDependencyResolver, String jsonSchemaPath, Object validatorData, JsonNode jsonNode, String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);
		
		if (!(validatorData instanceof JsonArray)) {
			throw new JsonSchemaDefinitionError("Data for required property keys is not a JsonArray", jsonSchemaPath);
    	}
	}
	
	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		if (!(jsonNode.isJsonObject())) {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				throw new JsonSchemaDataValidationError("Expected data type 'object' but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
			}
		} else {
			for (Object propertyKey : (JsonArray) validatorData) {
				if (propertyKey == null) {
					throw new JsonSchemaDefinitionError("Data entry for required property key name must be 'string' but was 'null'", jsonSchemaPath);
				} else if (!(propertyKey instanceof String)) {
					throw new JsonSchemaDefinitionError("Data entry for required property key name must be 'string' but was '" + propertyKey.getClass().getSimpleName() + "'", jsonSchemaPath);
				} else if (!((JsonObject) jsonNode.getValue()).containsPropertyKey((String) propertyKey)) {
					throw new JsonSchemaDataValidationError("Invalid property key. Missing required property '" + propertyKey + "'", jsonPath);
				}
			}
		}
    }
}
