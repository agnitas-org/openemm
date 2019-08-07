/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ParameterParser {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ParameterParser.class);
	
	private String parameterString;
	private int position;

	public ParameterParser(String parameterString) {
		this.parameterString = parameterString;
	}

	public Map<String, String> parse() {
		Map<String, String> parameters = new HashMap<>();
		
		if (StringUtils.isNotBlank(parameterString)) {
			try {
				parse(parameters, null);
			} catch (Exception e) {
				logger.error("Error occurred during parameters parsing: " + e.getMessage(), e);
				return null;
			}
		}

		return parameters;
	}

	public String parse(String requiredParameterName) {
		try {
			return parse(null, requiredParameterName);
		} catch (Exception e) {
			logger.error("Error occurred during parameters parsing: " + e.getMessage(), e);
			return null;
		}
	}

	private String parse(Map<String, String> parameters, String required) throws Exception {
		if (parameterString.contains("reply=\"\"") && !parameterString.contains("reply=\"\",")) {
			// TODO: remove after version 17.07.142+ is in the wild 
			// Temporarily needed bugfix for EMM-4929: Invalid escaping of reply address name
			parameterString = parameterString
				.replace("reply=\"\"", "reply=\"\\\"")
				.replace("\" <", "\\\" <")
				.replace("\\\\\" <", "\\\" <");
		}
		
		position = 0;

		while (skipWhitespace(true)) {
			String name = parseName();
			skipWhitespace();
			parseCharacter('=');
			skipWhitespace();
			String value = parseValue();

			if (StringUtils.equals(name, required)) {
				return value;
			}
			if (parameters != null) {
				parameters.put(name, value);
			}
		}

		return null;
	}

	private boolean skipWhitespace() {
		return skipWhitespace(false);
	}

	private boolean skipWhitespace(boolean skipSeparators) {
		while (position < parameterString.length()) {
			char c = parameterString.charAt(position);
			if (Character.isWhitespace(c)) {
				position++;
			} else if (skipSeparators && (c == ',' || c == ';')) {
				position++;
			} else {
				return true;
			}
		}
		return false;
	}

	private String parseName() throws Exception {
		StringBuilder builder = new StringBuilder();
		while (position < parameterString.length()) {
			char c = parameterString.charAt(position);
			if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
				position++;
				builder.append(c);
			} else if (Character.isWhitespace(c) || c == '=') {
				break;
			} else {
				throw new Exception("Unexpected character `" + c + "` at position " + position + ", whitespace or `=` is expected");
			}
		}

		if (builder.length() > 0) {
			return builder.toString();
		} else {
			throw new Exception("Parameter name missing");
		}
	}

	private String parseValue() throws Exception {
		StringBuilder builder = new StringBuilder();
		boolean escape = false;

		parseCharacter('"');

		while (position < parameterString.length()) {
			char c = parameterString.charAt(position);
			position++;

			if (escape) {
				escape = false;
				builder.append(c);
			} else if (c == '\\') {
				escape = true;
			} else if (c == '"') {
				return builder.toString();
			} else {
				builder.append(c);
			}
		}

		throw new Exception("Missing parameter value closing quotation mark");
	}

	private void parseCharacter(char c) throws Exception {
		if (position < parameterString.length()) {
			if (parameterString.charAt(position) == c) {
				position++;
				return;
			}
		}
		throw new Exception("Missing expected `" + c + "` character at position " + position);
	}

	public static String escapeValue(String input) {
		if (input == null) {
			return "";
		} else {
			input = StringUtils.replace(input, "\\", "\\\\");
			input = StringUtils.replace(input, "=", "\\=");
			input = StringUtils.replace(input, "\"", "\\\"");
			input = StringUtils.replace(input, ",", "\\,");
	
			return input;
		}
	}
}
