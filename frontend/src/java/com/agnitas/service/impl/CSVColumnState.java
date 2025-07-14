/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

public class CSVColumnState {
    public static final int TYPE_CHAR = 1;

    public static final int TYPE_NUMERIC = 2;

    public static final int TYPE_DATE = 3;

    private String colName;

    private Boolean importedColumn;

    private int type;

	private boolean nullable;

    public CSVColumnState() {
    }

    public CSVColumnState(String colName, Boolean importedColumn, int type) {
        this.colName = colName;
        this.importedColumn = importedColumn;
        this.type = type;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public Boolean getImportedColumn() {
        return importedColumn;
    }

    public void setImportedColumn(Boolean importedColumn) {
        this.importedColumn = importedColumn;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
}
