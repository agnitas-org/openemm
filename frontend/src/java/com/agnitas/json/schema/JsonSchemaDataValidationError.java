/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema;

public class JsonSchemaDataValidationError extends Exception {
	private static final long serialVersionUID = -4849599671599546633L;

	private final String jsonDataPath;

	public JsonSchemaDataValidationError(final String message, final String jsonDataPath) {
		super(message);

		this.jsonDataPath = jsonDataPath;
	}

	public JsonSchemaDataValidationError(final String message, final String jsonDataPath, final Exception e) {
		super(message, e);

		this.jsonDataPath = jsonDataPath;
	}

	public String getJsonDataPath() {
		return jsonDataPath;
	}

	@Override
	public String getMessage() {
		return "Invalid JSON data: " + super.getMessage() + (jsonDataPath == null ? "" : " at JSON path: " + jsonDataPath);
	}
}
