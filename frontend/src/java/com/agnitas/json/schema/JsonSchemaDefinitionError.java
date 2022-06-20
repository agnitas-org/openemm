/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema;

public class JsonSchemaDefinitionError extends Exception {
	private static final long serialVersionUID = 571902904309032324L;

	private final String jsonSchemaPath;

	public JsonSchemaDefinitionError(final String message, final String jsonSchemaPath) {
		super(message);

		this.jsonSchemaPath = jsonSchemaPath;
	}

	public JsonSchemaDefinitionError(final String message, final String jsonSchemaPath, final Exception e) {
		super(message, e);

		this.jsonSchemaPath = jsonSchemaPath;
	}

	public String getJsonSchemaPath() {
		return jsonSchemaPath;
	}

	@Override
	public String getMessage() {
		return "Invalid JSON schema definition: " + super.getMessage() + (jsonSchemaPath == null ? "" : " at JSON schema path: " + jsonSchemaPath);
	}
}
