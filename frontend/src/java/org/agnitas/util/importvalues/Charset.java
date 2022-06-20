/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.importvalues;

/**
 * Values for charset property of import profile
 */
public enum Charset {
	ISO_8859_1("ISO-8859-1", 0),
	UTF_8("UTF-8", 1),
	CHINESE_SIMPLIFIED("GB2312", 2),
	ISO_8859_15("ISO-8859-15", 3),
	ISO_8859_2("ISO-8859-2", 4),
	ISO_2022_JP("ISO-2022-JP", 5);

	/**
	 * value that is used during import
	 */
	private String charsetName;

	/**
	 * ID used for db storage
	 */
	private int id;

	public String getCharsetName() {
		return charsetName;
	}

	public int getIntValue() {
		return id;
	}

	Charset(String charsetName, int id) {
		this.charsetName = charsetName;
		this.id = id;
	}

	public static Charset getCharsetById(int id) throws Exception {
		for (Charset item : Charset.values()) {
			if (item.getIntValue() == id) {
				return item;
			}
		}
		throw new Exception("Invalid int value for Charset: " + id);
	}

	public static Charset getCharsetByName(String value) throws Exception {
		for (Charset item : Charset.values()) {
			if (item.charsetName.equalsIgnoreCase(value)) {
				return item;
			}
		}
		throw new Exception("Invalid value for Charset: " + value);
	}
}
