/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse Strings like:
 * 		name="value"
 * 		name="value value"
 * 		name1="value" name2="value value"
 */
public class AttributeParser {
	private static char NAME_VALUE_SEPARATOR = '=';
	private static char SINGLE_QUOTE = '\'';
	private static char DOUBLE_QUOTE = '"';
	private static List<Character> BLANK_CHARACTERS = Arrays.asList(' ', '\t', '\n', '\r', '\f');
	
	public static Map<String, String> parse(String data) throws Exception {
		Map<String, String> attributesFound = new HashMap<>();
		
		boolean insideQuotedValue = false;
		char quoteChar = '"';
		StringBuilder nextAttributeName = new StringBuilder();
		StringBuilder nextAttributeValue = null;

		data = data.trim();
		for (char nextChar : data.toCharArray()) {
			if (nextAttributeValue == null) {
				// Reading next attribute name
				if (SINGLE_QUOTE == nextChar || DOUBLE_QUOTE == nextChar) {
					// Switch to reading attribute value
					nextAttributeValue = new StringBuilder();
					insideQuotedValue = true;
					quoteChar = nextChar;
				} else if (NAME_VALUE_SEPARATOR == nextChar) {
					// Switch to reading attribute value
					nextAttributeValue = new StringBuilder();
				} else if (BLANK_CHARACTERS.contains(nextChar)) {
					// Ignore blank character outside of values
				} else {
					// Extend attribute name with next character
					nextAttributeName.append(nextChar);
				}
			} else {
				// Reading next attribute value
				if (SINGLE_QUOTE == nextChar || DOUBLE_QUOTE == nextChar) {
					if (!insideQuotedValue) {
						if (nextAttributeValue.length() > 0) {
							throw new Exception("Invalid attribute (value contains quote but was not started with quote): " + nextAttributeName.toString());
						}
						
						// Start quoted value
						insideQuotedValue = true;
						quoteChar = nextChar;
					} else if (quoteChar == nextChar) {
						// End quoted value
						attributesFound.put(nextAttributeName.toString(), nextAttributeValue.toString());
						insideQuotedValue = false;
						nextAttributeName = new StringBuilder();
						nextAttributeValue = null;
					} else {
						// Extend attribute value with next character
						nextAttributeValue.append(nextChar);
					}
				} else if (NAME_VALUE_SEPARATOR == nextChar) {
					if (!insideQuotedValue) {
						throw new Exception("Invalid attribute (value was not quoted but contains equal sign): " + nextAttributeName.toString());
					} else {
						nextAttributeValue.append(nextChar);
					}
				} else if (BLANK_CHARACTERS.contains(nextChar)) {
					if (insideQuotedValue) {
						// Extend attribute value with blank character
						nextAttributeValue.append(nextChar);
					} else {
						// Ignore blank character outside of values, but use it as attribute separator after value has started
						if (nextAttributeValue.length() > 0) {
							attributesFound.put(nextAttributeName.toString(), nextAttributeValue.toString());
							insideQuotedValue = false;
							nextAttributeName = new StringBuilder();
							nextAttributeValue = null;
						}
					}
				} else {
					// Extend attribute value with next character
					nextAttributeValue.append(nextChar);
				}
			}
		}
		
		if (insideQuotedValue) {
			throw new Exception("Invalid attribute (value started with quote but was not closed): " + nextAttributeName.toString());
		} else if (nextAttributeName.length() > 0) {
			// Work on last attribute with non-quoted value
			if (nextAttributeValue != null && nextAttributeValue.length() > 0) {
				attributesFound.put(nextAttributeName.toString(), nextAttributeValue.toString());
			} else {
				throw new Exception("Invalid attribute (has no value): " + nextAttributeName.toString());
			}
		}
		
		return attributesFound;
	}
}
