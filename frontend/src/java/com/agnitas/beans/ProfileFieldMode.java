/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

public enum ProfileFieldMode {
	/**
	 * Show this field in GUI for edit (StorageCode 0)
	 */
	Editable(0),
	
	/**
	 * This field is shown but cannot be edited by the user (StorageCode 1)
	 */
	ReadOnly(1),
	
	/**
	 * This field is not intended to be shown in the GUI (StorageCode 2)
	 */
	NotVisible(2);
	
	private int storageCode;

	public int getStorageCode() {
		return storageCode;
	}

	ProfileFieldMode(int storageCode) {
		this.storageCode = storageCode;
	}

	public static ProfileFieldMode getProfileFieldModeForStorageCode(int storageCode) throws Exception {
		for (ProfileFieldMode profileFieldMode : ProfileFieldMode.values()) {
			if (profileFieldMode.storageCode == storageCode) {
				return profileFieldMode;
			}
		}
		throw new Exception("Unknown storage code for ProfileFieldMode: " + storageCode);
	}

	public static ProfileFieldMode getProfileFieldModeForName(String name) throws Exception {
		for (ProfileFieldMode profileFieldMode : ProfileFieldMode.values()) {
			if (profileFieldMode.name().equalsIgnoreCase(name)) {
				return profileFieldMode;
			}
		}
		throw new Exception("Unknown name for ProfileFieldMode: " + name);
	}
}
