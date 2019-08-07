/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.importvalues;

import org.apache.commons.lang.StringUtils;

/**
 * Values for textRecognitionChar property of import profile
 */
public enum TextRecognitionChar {
	NONE(null, "delimiter.none", 0),
	DOUBLE_QUOTE('\"', "delimiter.doublequote", 1),
	SINGLE_QUOTE('\'', "delimiter.singlequote", 2);

	/**
	 * Character as String that will be used during csv-file parsing
	 */
	private Character value;

	/**
	 * message key in resource bundle to display value on pages
	 */
	private String messageKey;

	/**
	 * id value for storage in db
	 */
	private int id;
	
	public Character getValueCharacter() {
		return value;
	}
	
	public String getValueString() {
		if (value == null) {
			return "";
		} else {
			return value.toString();
		}
	}

	public String getPublicValue() {
		return messageKey;
	}

	public int getIntValue() {
		return id;
	}

	TextRecognitionChar(Character value, String messageKey, int id) {
		this.value = value;
		this.messageKey = messageKey;
		this.id = id;
	}

	public static TextRecognitionChar getTextRecognitionCharById(int id) throws Exception {
		for (TextRecognitionChar item : TextRecognitionChar.values()) {
			if (item.getIntValue() == id) {
				return item;
			}
		}
		throw new Exception("Invalid int value for TextRecognitionChar");
	}

	public static TextRecognitionChar getTextRecognitionCharByChar(Character textRecognitionChar) throws Exception {
		for (TextRecognitionChar item : TextRecognitionChar.values()) {
			if (item.getValueCharacter() == null && textRecognitionChar == null) {
				return item;
			} else if (item.getValueCharacter() != null && item.getValueCharacter().equals(textRecognitionChar)) {
				return item;
			}
		}
		throw new Exception("Invalid TextRecognitionChar char");
	}

	public static TextRecognitionChar getTextRecognitionCharByString(String textRecognitionCharString) throws Exception {
		if (StringUtils.isEmpty(textRecognitionCharString)) {
			return NONE;
		} else if (textRecognitionCharString.length() > 1) {
			throw new Exception("Invalid TextRecognitionChar char");
		} else {
			return getTextRecognitionCharByChar(textRecognitionCharString.charAt(0));
		}
	}
}
