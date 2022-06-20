/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;

import com.agnitas.json.JsonNode;
import com.agnitas.json.schema.JsonSchemaDataValidationError;
import com.agnitas.json.schema.JsonSchemaDefinitionError;
import com.agnitas.json.schema.JsonSchemaDependencyResolver;

public class FormatValidator extends BaseJsonSchemaValidator {
	public FormatValidator(final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String jsonSchemaPath, final Object validatorData, final JsonNode jsonNode, final String jsonPath) throws JsonSchemaDefinitionError {
		super(jsonSchemaDependencyResolver, jsonSchemaPath, validatorData, jsonNode, jsonPath);

		if (validatorData == null) {
			throw new JsonSchemaDefinitionError("Format value is 'null'", jsonSchemaPath);
		} else if (!(validatorData instanceof String)) {
			throw new JsonSchemaDefinitionError("Format value is not a string", jsonSchemaPath);
		}
	}

	@Override
	public void validate() throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		if (!jsonNode.isString()) {
			throw new JsonSchemaDataValidationError("Expected a 'string' value for formatcheck but was '" + jsonNode.getJsonDataType().getName() + "'", jsonPath);
		} else if ("email".equalsIgnoreCase((String) validatorData)) {
			if (!AgnUtils.isEmailValid((String) jsonNode.getValue())) {
				throw new JsonSchemaDataValidationError("Invalid data for format '" + ((String) validatorData) + "' was '" + jsonNode.getValue() + "'", jsonPath);
			}
		} else if ("date-time".equalsIgnoreCase((String) validatorData)) {
			try {
				new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT).parse((String) jsonNode.getValue());
			} catch (final ParseException e) {
				throw new JsonSchemaDataValidationError("Invalid data for format '" + ((String) validatorData) + "' was '" + jsonNode.getValue() + "'", jsonPath, e);
			}
		} else if ("hostname".equalsIgnoreCase((String) validatorData)) {
			if (!NetworkUtilities.isValidHostname((String) jsonNode.getValue())) {
				throw new JsonSchemaDataValidationError("Invalid data for format '" + ((String) validatorData) + "' was '" + jsonNode.getValue() + "'", jsonPath);
			}
		} else if ("ipv4".equalsIgnoreCase((String) validatorData)) {
			if (!NetworkUtilities.isValidIpV4((String) jsonNode.getValue())) {
				throw new JsonSchemaDataValidationError("Invalid data for format '" + ((String) validatorData) + "' was '" + jsonNode.getValue() + "'", jsonPath);
			}
		} else if ("ipv6".equalsIgnoreCase((String) validatorData)) {
			if (!NetworkUtilities.isValidIpV6((String) jsonNode.getValue())) {
				throw new JsonSchemaDataValidationError("Invalid data for format '" + ((String) validatorData) + "' was '" + jsonNode.getValue() + "'", jsonPath);
			}
		} else if ("uri".equalsIgnoreCase((String) validatorData)) {
			if (!NetworkUtilities.isValidUri((String) jsonNode.getValue())) {
				throw new JsonSchemaDataValidationError("Invalid data for format '" + ((String) validatorData) + "' was '" + jsonNode.getValue() + "'", jsonPath);
			}
		} else if ("regex".equalsIgnoreCase((String) validatorData)) {
			try {
				Pattern.compile((String) jsonNode.getValue());
			} catch (final Exception e) {
				throw new JsonSchemaDataValidationError("Invalid data for format '" + ((String) validatorData) + "' was '" + jsonNode.getValue() + "'", jsonPath, e);
			}
		} else if ("base64".equalsIgnoreCase((String) validatorData)) {
			if (!isValidBase64((String) jsonNode.getValue())) {
				throw new JsonSchemaDataValidationError("Invalid data for format '" + ((String) validatorData) + "' was '" + AgnUtils.shortenStringToMaxLength((String) jsonNode.getValue(), 20) + "'", jsonPath);
			}
		} else {
			throw new JsonSchemaDefinitionError("Unknown format name '" + validatorData + "'", jsonSchemaPath);
		}
	}
	
	public static boolean isValidBase64(final String value) {
		return Pattern.matches("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$", value);
	}
}
