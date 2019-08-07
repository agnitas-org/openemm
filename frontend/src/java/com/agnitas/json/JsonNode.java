/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json;

import java.math.BigDecimal;

public class JsonNode {
	private Object value;
	private JsonDataType jsonDataType;
		
	public JsonNode(Object value) throws Exception {
		this.value = value;
		if (value == null) {
			jsonDataType = JsonDataType.NULL;
		} else if (value instanceof Boolean) {
			jsonDataType = JsonDataType.BOOLEAN;
		} else if (value instanceof Integer || value instanceof Long || (value instanceof BigDecimal && ((BigDecimal) value).scale() == 0)) {
			jsonDataType = JsonDataType.INTEGER;
		} else if (value instanceof Number) {
			jsonDataType = JsonDataType.NUMBER;
		} else if (value instanceof String || value instanceof Character) {
			jsonDataType = JsonDataType.STRING;
		} else if (value instanceof JsonObject) {
			jsonDataType = JsonDataType.OBJECT;
		} else if (value instanceof JsonArray) {
			jsonDataType = JsonDataType.ARRAY;
		} else {
			throw new Exception("Unknown JSON data type: " + value.getClass().getSimpleName());
		}
	}
	
	public Object getValue() {
		return value;
	}

	public JsonDataType getJsonDataType() {
		return jsonDataType;
	}
	
	public boolean isNull() {
		return jsonDataType == JsonDataType.NULL;
	}

	public boolean isBoolean() {
		return jsonDataType == JsonDataType.BOOLEAN;
	}

	public boolean isInteger() {
		return jsonDataType == JsonDataType.INTEGER;
	}

	public boolean isNumber() {
		return jsonDataType == JsonDataType.NUMBER || jsonDataType == JsonDataType.INTEGER;
	}

	public boolean isString() {
		return jsonDataType == JsonDataType.STRING;
	}
	
	public boolean isJsonObject() {
		return jsonDataType == JsonDataType.OBJECT;
	}
	
	public boolean isJsonArray() {
		return jsonDataType == JsonDataType.ARRAY;
	}

	public boolean isSimpleValue() {
		return jsonDataType == JsonDataType.NULL
			|| jsonDataType == JsonDataType.STRING
			|| jsonDataType == JsonDataType.BOOLEAN
			|| jsonDataType == JsonDataType.INTEGER
			|| jsonDataType == JsonDataType.NUMBER;
	}

	public boolean isComplexValue() {
		return jsonDataType == JsonDataType.OBJECT
			|| jsonDataType == JsonDataType.ARRAY;
	}

	@Override
	public String toString() {
		if (isNull()) {
			return "null";
		} else {
			return getValue().toString();
		}
	}
}
