/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.importvalues;

import org.apache.commons.lang.StringUtils;

public enum Gender {
	MALE(0),
	FEMALE(1),
	UNKNOWN(2),
	FEMALE2(3), // Fräulein, Ms. (optional)
	PRAXIS(4), // Doctor's office (optional)
	COMPANY(5);

	/**
	 * ID used for db storage
	 */
	private int storageValue;

	public int getStorageValue() {
		return storageValue;
	}

	Gender(int storageValue) {
		this.storageValue = storageValue;
	}

	public static Gender getGenderById(int storageValue) throws Exception {
		for (Gender item : Gender.values()) {
			if (item.getStorageValue() == storageValue) {
				return item;
			}
		}
		throw new Exception("Invalid int value for Gender: " + storageValue);
	}
	
	public static Gender getGenderByDefaultGenderMapping(String value) {
		if (StringUtils.isBlank(value)) {
			return UNKNOWN;
		} else {
			switch (value.toLowerCase().trim()) {
				case "herr":
				case "herrn":
				case "hr.":
				case "hr":
				case "mister":
				case "mr":
				case "mr.":
				case "m":
				case "männlich":
				case "maennlich":
				case "male":
				case "monsieur":
				case "mâle":
				case "m.":
					return MALE;
				case "frau":
				case "fr.":
				case "fr":
				case "fräulein":
				case "fraeulein":
				case "frl.":
				case "frl":
				case "miss":
				case "ms":
				case "ms.":
				case "misses":
				case "mrs":
				case "mrs.":
				case "w":
				case "weiblich":
				case "f":
				case "female":
				case "femme":
				case "femelle":
				case "mademoiselle":
				case "madame":
				case "mme":
				case "melle":
					return FEMALE;
				default:
					return UNKNOWN;
			}
		}
	}
}
