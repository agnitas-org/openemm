/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class EnumValidator extends BaseJsonSchemaValidator {
    public EnumValidator(JsonSchemaDependencyResolver jsonSchemaDependencyResolver, String jsonSchemaPath, Object validatorData, JsonNode jsonNode, String jsonPath) throws JsonSchemaDefinitionError {
    	super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);
    	
    	if (validatorData == null) {
    		throw new JsonSchemaDefinitionError("Enum data is 'null'", jsonSchemaPath);
    	} else if (!(validatorData instanceof JsonArray)) {
    		throw new JsonSchemaDefinitionError("Enum contains a non-JsonArray", jsonSchemaPath);
    	} else if (((JsonArray) validatorData).size() == 0) {
    		throw new JsonSchemaDefinitionError("Enum contains an empty JsonArray", jsonSchemaPath);
    	}
    }
	
	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		for (Object enumObject : ((JsonArray) validatorData)) {
			if (jsonNode.isNull() && enumObject == null) {
				return;
			} else if (enumObject != null && enumObject.equals(jsonNode.getValue())) {
				return;
			}
		}
		throw new JsonSchemaDataValidationError("Enumeration expected one of '" + StringUtils.join((JsonArray) validatorData, "', '") + "' but was " + (jsonNode.isSimpleValue() ? "'" + jsonNode.getValue() + "'" : "'" + jsonNode.getJsonDataType() + "'"), jsonPath);
    }
}
