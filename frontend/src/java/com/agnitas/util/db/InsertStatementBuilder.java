/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.db;

import java.util.ArrayList;
import java.util.List;

public final class InsertStatementBuilder {

	private final String table;
	private final List<String> columns;
	private final List<Object> values;
	
	public InsertStatementBuilder(final String table) {
		this.table = table;
		
		this.columns = new ArrayList<>();
		this.values = new ArrayList<>();
	}
	
	public InsertStatementBuilder withPlaceholder(final String columnName, final Object value) {
		this.columns.add(columnName);
		this.values.add(value);
		
		return this;
	}
	
	public final String buildStatement() {
		final String placeholders = placeholders(columns.size());
		
		final String columnNameList = String.join(",", columns);
		
		return String.format("INSERT INTO %s (%s) VALUES (%s)", table, columnNameList, placeholders);
	}
	
	public final Object[] buildParameters() {
		return values.toArray();
	}
	
	private static final String placeholders(final int num) {
		final StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < num; i++) {
			sb.append(i > 0 ? ",?" : "?");
		}
		
		return sb.toString();
	}
}
