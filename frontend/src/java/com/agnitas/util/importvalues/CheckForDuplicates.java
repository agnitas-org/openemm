/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.importvalues;

/**
 * Values for checkForDuplicates property of import profile
 */
public enum CheckForDuplicates {

	COMPLETE(1, "default.Yes"),
	NO_CHECK(2, "default.No");

	/**
	 * int value for db storage
	 */
	private final int storageInt;

	/**
	 * message key in resource bundle to display value on pages
	 */
	private final String messageKey;

	public String getMessageKey() {
		return messageKey;
	}

	public int getIntValue() {
		return storageInt;
	}

	CheckForDuplicates(int storageInt, String messageKey) {
		this.storageInt = storageInt;
		this.messageKey = messageKey;
	}

	public static CheckForDuplicates getFromInt(int intValue) {
		for (CheckForDuplicates checkForDuplicates : CheckForDuplicates.values()) {
			if (checkForDuplicates.getIntValue() == intValue) {
				return checkForDuplicates;
			}
		}
		throw new IllegalArgumentException("Invalid int value for CheckForDuplicates");
	}
}
