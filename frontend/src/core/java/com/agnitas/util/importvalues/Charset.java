/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.importvalues;

/**
 * Values for charset property of import profile
 */
public enum Charset {

	ISO_8859_1("ISO-8859-1", 0, "mailing.iso-8859-1"),
	UTF_8("UTF-8", 1, "mailing.utf-8"),
	CHINESE_SIMPLIFIED("GB2312", 2, "mailing.gb2312"),
	ISO_8859_15("ISO-8859-15", 3, "mailing.iso-8859-15"),
	ISO_8859_2("ISO-8859-2", 4, "mailing.iso-8859-2"),
	ISO_2022_JP("ISO-2022-JP", 5, "mailing.iso-2022-jp");

	/**
	 * value that is used during import
	 */
	private final String charsetName;

	/**
	 * ID used for db storage
	 */
	private final int id;
	private final String messageKey;

	public String getCharsetName() {
		return charsetName;
	}

	public int getIntValue() {
		return id;
	}

	public String getMessageKey() {
		return messageKey;
	}

	Charset(String charsetName, int id, String messageKey) {
		this.charsetName = charsetName;
		this.id = id;
		this.messageKey = messageKey;
	}

	public static Charset getCharsetById(int id) {
		for (Charset item : Charset.values()) {
			if (item.getIntValue() == id) {
				return item;
			}
		}
		throw new IllegalArgumentException("Invalid int value for Charset: " + id);
	}

	public static Charset getCharsetByName(String value) {
		for (Charset item : Charset.values()) {
			if (item.charsetName.equalsIgnoreCase(value)) {
				return item;
			}
		}
		throw new IllegalArgumentException("Invalid value for Charset: " + value);
	}
}
