/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.importvalues;

/**
 * Values for separator property of import profile
 */
public enum Separator {

	SEMICOLON(';', "separator.semicolon", 0),
	COMMA(',', "separator.comma", 1),
	PIPE('|', "separator.pipe", 2),
	TAB('\t', "separator.tab", 3),
	CARET('^', "separator.caret", 4);

	/**
	 * value that is used during csv-file parsing
	 */
	private final char value;

	/**
	 * message key in resource bundle to display value on pages
	 */
	private final String messageKey;

	private final int id;

	Separator(char value, String messageKey, int id) {
		this.value = value;
		this.messageKey = messageKey;
		this.id = id;
	}

	public char getValueChar() {
		return value;
	}

	public String getValueString() {
		return Character.toString(value);
	}

	public String getPublicValue() {
		return messageKey;
	}

	public int getIntValue() {
		return id;
	}

	public static Separator getSeparatorById(int id) {
		for (Separator item : Separator.values()) {
			if (item.getIntValue() == id) {
				return item;
			}
		}
		throw new IllegalArgumentException("Invalid separator id: " + id);
	}

	public static Separator getSeparatorByChar(char separatorChar) {
		for (Separator separator : Separator.values()) {
			if (separator.getValueChar() == separatorChar) {
				return separator;
			}
		}
		throw new IllegalArgumentException("Invalid separator char: " + separatorChar);
	}
}
