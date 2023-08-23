/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

public enum GridDivContainerEditMode {
    EDITABLE_CONTAINER(0),
    FIXED_CONTAINER(1),
    ANCHORED_CONTAINER(2),
    FIXED_AND_ANCHORED_CONTAINER(3);

    private int storageCode;

    public int getStorageCode() {
        return storageCode;
    }

    GridDivContainerEditMode(int storageCode) {
        this.storageCode = storageCode;
    }

    public static GridDivContainerEditMode getDivContainerEditModeForStorageCode(int storageCode) throws Exception {
        for (GridDivContainerEditMode divContainerEditMode : GridDivContainerEditMode.values()) {
            if (divContainerEditMode.storageCode == storageCode) {
                return divContainerEditMode;
            }
        }
        throw new Exception("Unknown storage code for DivContainerEditMode: " + storageCode);
    }

    public static GridDivContainerEditMode getDivContainerEditModeForName(String name) throws Exception {
        for (GridDivContainerEditMode divContainerEditMode : GridDivContainerEditMode.values()) {
            if (divContainerEditMode.name().equalsIgnoreCase(name)) {
                return divContainerEditMode;
            }
        }
        throw new Exception("Unknown name for DivContainerEditMode: " + name);
    }
}
