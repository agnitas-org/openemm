/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RowsCountWebStorageEntry implements WebStorageEntry {

    private static final int DEFAULT_ROWS_COUNT = 50;

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
    public WebStorageEntry clone() throws CloneNotSupportedException {
        throw new UnsupportedOperationException();
    }

}
