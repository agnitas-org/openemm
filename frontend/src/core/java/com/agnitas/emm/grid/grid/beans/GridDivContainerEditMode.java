/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

public enum GridDivContainerEditMode {

    EDITABLE_CONTAINER(0, "grid.div.container.mode.editable"),
    FIXED_CONTAINER(1, "grid.div.container.mode.fixed"),
    ANCHORED_CONTAINER(2, "grid.div.container.mode.anchored"),
    FIXED_AND_ANCHORED_CONTAINER(3, "grid.div.container.mode.anchoredAndFixed");

    private final int storageCode;
    private final String messageKey;

    GridDivContainerEditMode(int storageCode, String messageKey) {
        this.storageCode = storageCode;
        this.messageKey = messageKey;
    }

    public int getStorageCode() {
        return storageCode;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public static GridDivContainerEditMode findModeByCode(int storageCode) {
        for (GridDivContainerEditMode divContainerEditMode : GridDivContainerEditMode.values()) {
            if (divContainerEditMode.storageCode == storageCode) {
                return divContainerEditMode;
            }
        }

        return null;
    }
}
