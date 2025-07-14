/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.importvalues;

import org.apache.commons.lang3.StringUtils;

public enum Gender {

	MALE(0),
	FEMALE(1),
	UNKNOWN(2),
	// FEMALE2(3), // Fräulein, Ms. (optional)
	PRAXIS(4), // Doctor's office (optional)
	COMPANY(5);

	/**
	 * ID used for db storage
	 */
	private final int storageValue;

	Gender(int storageValue) {
		this.storageValue = storageValue;
	}

	public int getStorageValue() {
		return storageValue;
	}

	public static Gender getGenderById(int storageValue) {
		for (Gender item : Gender.values()) {
			if (item.getStorageValue() == storageValue) {
				return item;
			}
		}
		if (storageValue == 3) {
			// Fallback for deprecated old value FEMALE2
			return FEMALE;
		} else {
			throw new IllegalArgumentException("Invalid int value for Gender: " + storageValue);
		}
	}
	
	public static Gender getGenderByDefaultGenderMapping(String value) {
		if (StringUtils.isBlank(value)) {
			return UNKNOWN;
		}

		switch (value.toLowerCase().trim()) {
			case "herr", "herrn", "hr.", "hr", "mister", "mr", "mr.", "m", "männlich", "maennlich", "male", "monsieur", "mâle", "m.":
				return MALE;
			case "frau", "fr.", "fr", "fräulein", "fraeulein", "frl.", "frl", "miss", "ms", "ms.", "misses", "mrs", "mrs.", "w",
				 "weiblich", "f", "female", "femme", "femelle", "mademoiselle", "madame", "mme", "melle":
				return FEMALE;
			default:
				return UNKNOWN;
		}
	}
}
