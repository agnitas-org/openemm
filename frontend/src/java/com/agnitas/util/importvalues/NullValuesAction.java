/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.importvalues;

/**
 * Values for nullValuesAction property of import profile
 */
public enum NullValuesAction {

	OVERWRITE(0, "import.dont_ignore_null_values"),
	IGNORE(1, "import.ignore_null_values");

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

	NullValuesAction(int storageInt, String messageKey) {
		this.storageInt = storageInt;
		this.messageKey = messageKey;
	}

	public static NullValuesAction getFromInt(int intValue) {
		for (NullValuesAction nullValuesAction : NullValuesAction.values()) {
			if (nullValuesAction.getIntValue() == intValue) {
				return nullValuesAction;
			}
		}
		throw new IllegalArgumentException("Invalid int value for NullValuesAction");
	}
}
