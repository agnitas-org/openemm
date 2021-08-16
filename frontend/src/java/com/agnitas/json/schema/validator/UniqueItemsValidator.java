/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class UniqueItemsValidator extends BaseJsonSchemaValidator {
	public UniqueItemsValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("Data for 'uniqueItems' items is 'null'", jsonSchemaPath);
		} else if (validatorData instanceof Boolean) {
			this.validatorData = validatorData;
		} else if (validatorData instanceof String) {
			try {
				this.validatorData = Boolean.parseBoolean((String) validatorData);
			} catch (final NumberFormatException e) {
				throw new JsonSchemaDefinitionError("Data for 'uniqueItems' items is '" + validatorData + "' and not 'boolean'", jsonSchemaPath, e);
			}
		} else {
			throw new JsonSchemaDefinitionError("Data for 'uniqueItems' is not 'boolean'", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		if (!(jsonNode.isJsonArray())) {
			if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
				throw new JsonSchemaDataValidationError("Expected data type 'array' but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
			}
		} else {
			if ((Boolean) validatorData) {
				final JsonArray jsonArray = (JsonArray) jsonNode.getValue();
				for (int i = 0; i < jsonArray.size(); i++) {
					for (int j = i + 1; j < jsonArray.size(); j++) {
						if ((jsonArray.get(i) == jsonArray.get(j))
								|| (jsonArray.get(i) != null && jsonArray.get(i).equals(jsonArray.get(j)))) {
							throw new JsonSchemaDataValidationError("Item '" + jsonArray.get(i) + "' of array is not unique", jsonPath);
						}
					}
				}
			}
		}
	}
}
