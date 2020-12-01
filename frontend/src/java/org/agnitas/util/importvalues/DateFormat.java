/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.importvalues;

import org.agnitas.util.DateUtilities;

/**
 * Values for dateFormat property of import profile
 */
public enum DateFormat {
	ddMMyyyyHHmm("dd.MM.yyyy HH:mm", "import.date.format.ddMMyyyyHHmm", 0),
	ddMMyyyy("dd.MM.yyyy", "import.date.format.ddMMyyyy", 1),
	yyyyMMdd("yyyyMMdd", "import.date.format.yyyyMMdd", 2),
	yyyyMMddHHmm("yyyyMMdd HH:mm", "import.date.format.yyyyMMddHHmm", 3),
	yyyyMMddHHmmss("yyyy-MM-dd HH:mm:ss", "import.date.format.yyyyMMddHHmmss", 4),
	ddMMyyyyHHmmss("dd.MM.yyyy HH:mm:ss", "import.date.format.ddMMyyyyHHmmss", 5),
	ddMMyy("dd.MM.yy", "import.date.format.ddMMyy", 6),
	MM_dd_yyyy_hh_mm_ss("MM/dd/yyyy hh:mm:ss", "import.date.format.MM_dd_yyyy_hh_mm_ss", 7),
	yyyy_MM_dd("yyyy-MM-dd", "import.date.format.yyyy_MM_dd", 8),
	ISO8601(DateUtilities.ISO_8601_DATETIME_FORMAT, "import.date.format.iso8601", 9);

	/**
	 * The value that will be used during csv-file parsing
	 */
	private String value;

	/**
	 * message key in resource bundle to display value on pages
	 */
	private String messageKey;
	
	/**
	 * Id value used for storage in db
	 */
	private int id;

	public String getValue() {
		return value;
	}

	public String getPublicValue() {
		return messageKey;
	}

	public int getIntValue() {
		return id;
	}

	DateFormat(String value, String messageKey, int id) {
		this.value = value;
		this.messageKey = messageKey;
		this.id = id;
	}

	public static DateFormat getDateFormatById(int id) throws Exception {
		for (DateFormat item : DateFormat.values()) {
			if (item.getIntValue() == id) {
				return item;
			}
		}
		throw new Exception("Invalid DateFormat id: " + id);
	}

	public static DateFormat getDateFormatByMessageKey(String messageKey) throws Exception {
		for (DateFormat item : DateFormat.values()) {
			if (item.messageKey.equals(messageKey)) {
				return item;
			}
		}
		throw new Exception("Invalid DateFormat messageKey: " + messageKey);
	}

	public static DateFormat getDateFormatByValue(String value) throws Exception {
		for (DateFormat item : DateFormat.values()) {
			if (item.value.equals(value)) {
				return item;
			}
		}
		throw new Exception("Invalid DateFormat value: " + value);
	}
}
