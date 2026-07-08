/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.auto_import.enums;

import com.agnitas.beans.IntEnum;

/**
 * An ids are used for status representation in database.
 * So make sure that ids are unique and keep in mind to never change them once assigned.
 */
public enum AutoImportJobStatus implements IntEnum {
    QUEUED("Queued", 1),
    RUNNING("Running", 2),
    TRANSFERRING("Transferring", 3),
    DONE("Done", 4),
    FAILED("Failed", 5);

    private String description;
    private int id;

    public static AutoImportJobStatus fromId(int id) {
        return IntEnum.fromId(AutoImportJobStatus.class, id);
    }

    public static AutoImportJobStatus fromId(int id, boolean safe) {
        return IntEnum.fromId(AutoImportJobStatus.class, id, safe);
    }

    AutoImportJobStatus(String description, int id) {
        this.description = description;
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int getId() {
        return id;
    }
}
