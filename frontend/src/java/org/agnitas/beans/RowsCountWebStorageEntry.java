/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import org.agnitas.web.forms.StrutsFormBase;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RowsCountWebStorageEntry implements WebStorageEntry {
    private static final int DEFAULT_ROWS_COUNT = StrutsFormBase.DEFAULT_NUMBER_OF_ROWS;

    @JsonProperty("rows-count")
    private int rowsCount = DEFAULT_ROWS_COUNT;

    public int getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(int rowsCount) {
        if (rowsCount > 0) {
            this.rowsCount = rowsCount;
        } else {
            this.rowsCount = DEFAULT_ROWS_COUNT;
        }
    }

    @Override
    public RowsCountWebStorageEntry clone() throws CloneNotSupportedException {
        RowsCountWebStorageEntry entry = (RowsCountWebStorageEntry) super.clone();
        entry.setRowsCount(rowsCount);
        return entry;
    }
}
