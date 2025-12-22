/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.importvalues;

import com.agnitas.util.DateUtilities;

/**
 * Values for dateFormat property of import profile
 */
public enum DateFormat {

	ddMMyyyy("dd.MM.yyyy", 1),
	ddMMyyyyHHmm("dd.MM.yyyy HH:mm", 0),
	ddMMyyyyHHmmss("dd.MM.yyyy HH:mm:ss", 5),

	ddMMyy("dd.MM.yy", 6),
	ddMMyyHHmm("dd.MM.yy HH:mm", 13),
	ddMMyyHHmmss("dd.MM.yy HH:mm:ss", 14),
	
	MMddyyyy("MM/dd/yyyy", 11),
	MMddyyyyhhmm("MM/dd/yyyy HH:mm", 10),
	MMddyyyyhhmmss("MM/dd/yyyy HH:mm:ss", 7),
	
	MMddyy("MM/dd/yy", 12),
	MMddyyhhmm("MM/dd/yy HH:mm", 15),
	MMddyyhhmmss("MM/dd/yy HH:mm:ss", 16),
	
	yyyyMMdd("yyyyMMdd", 2),
	yyyy_MM_dd("yyyy-MM-dd", 8),
	yyyyMMddHHmm("yyyyMMdd HH:mm", 3),
	yyyyMMddHHmmss("yyyy-MM-dd HH:mm:ss", 4),
	
	ISO8601(DateUtilities.ISO_8601_DATETIME_FORMAT, 9);

	/**
	 * The value that will be used during csv-file parsing
	 */
	private final String value;

	/**
	 * Id value used for storage in db
	 */
	private final int id;

	public String getValue() {
		return value;
	}

	public String getPublicValue() {
		return value;
	}

	public int getIntValue() {
		return id;
	}

	DateFormat(String value, int id) {
		this.value = value;
		this.id = id;
	}

	public static DateFormat getDateFormatById(int id) {
		for (DateFormat item : DateFormat.values()) {
			if (item.getIntValue() == id) {
				return item;
			}
		}
		throw new IllegalArgumentException("Invalid DateFormat id: " + id);
	}

	public static DateFormat getDateFormatByValue(String value) {
		for (DateFormat item : DateFormat.values()) {
			if (item.value.equals(value)) {
				return item;
			}
		}
		throw new IllegalArgumentException("Invalid DateFormat value: " + value);
	}
}
