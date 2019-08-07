/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database.impl;

class TableAndColumn {

	private final String tableName;
	private final String columnName;
	
	public TableAndColumn(final String tableName, final String columnName) {
		this.tableName = tableName;
		this.columnName = columnName;
	}
	
	@Override
	public int hashCode() {
		return tableName.hashCode() + columnName.hashCode();
	}
	
	@Override
	public boolean equals(final Object o) {
		if(!(o instanceof TableAndColumn)) { 
			return false;
		} else {
			final TableAndColumn c = (TableAndColumn) o;
			
			return c.columnName.equals(columnName) && c.tableName.equals(tableName);
		}
	}
}
