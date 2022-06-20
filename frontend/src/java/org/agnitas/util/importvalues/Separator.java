/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.importvalues;

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
	private char value;

	/**
	 * message key in resource bundle to display value on pages
	 */
	private String messageKey;

	private int id;

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

	Separator(char value, String messageKey, int id) {
		this.value = value;
		this.messageKey = messageKey;
		this.id = id;
	}

	public static Separator getSeparatorById(int id) throws Exception {
		for (Separator item : Separator.values()) {
			if (item.getIntValue() == id) {
				return item;
			}
		}
		throw new Exception("Invalid separator id: " + id);
	}

	public static Separator getSeparatorByChar(char separatorChar) throws Exception {
		for (Separator separator : Separator.values()) {
			if (separator.getValueChar() == separatorChar) {
				return separator;
			}
		}
		throw new Exception("Invalid separator char: " + separatorChar);
	}

	public static char getSeparatorCharByName(String separatorName) throws Exception {
		for (Separator separator : Separator.values()) {
			if (separator.getPublicValue().equalsIgnoreCase(separatorName)) {
				return separator.getValueChar();
			}
		}
		throw new Exception("Unknown separator name: " + separatorName);
	}
}
