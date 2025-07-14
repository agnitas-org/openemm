/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.util.regex.Pattern;

import com.agnitas.json.JsonNode;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class PatternValidator extends BaseJsonSchemaValidator {
    public PatternValidator(JsonSchemaDependencyResolver jsonSchemaDependencyResolver, String jsonSchemaPath, Object validatorData, JsonNode jsonNode, String jsonPath) throws JsonSchemaDefinitionError {
    	super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);
    	
    	if (!(validatorData instanceof String)) {
    		throw new JsonSchemaDefinitionError("Pattern is no string", jsonSchemaPath);
    	}
    }
	
	@Override
	public void validate() throws JsonSchemaDataValidationError {
		Pattern pattern = Pattern.compile((String) validatorData);
		if (jsonNode.isNumber()) {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				if (!pattern.matcher(((Number) jsonNode.getValue()).toString()).find()) {
					throw new JsonSchemaDataValidationError("RegEx pattern '" + (String) validatorData + "' is not matched by data number '" + jsonNode.getValue() + "'", jsonPath);
				}
			}
		} else if (jsonNode.isBoolean()) {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				if (!pattern.matcher(((Boolean) jsonNode.getValue()).toString()).find()) {
					throw new JsonSchemaDataValidationError("RegEx pattern '" + (String) validatorData + "' is not matched by data boolean '" + jsonNode.getValue() + "'", jsonPath);
				}
			}
		} else if (jsonNode.isString()) {
			if (!pattern.matcher((String) jsonNode.getValue()).find()) {
				throw new JsonSchemaDataValidationError("RegEx pattern '" + (String) validatorData + "' is not matched by data string '" + (String) jsonNode.getValue() + "'", jsonPath);
			}
		} else {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				throw new JsonSchemaDataValidationError("Expected data type 'string' or 'number' or 'boolean' but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
			}
		}
    }
}
