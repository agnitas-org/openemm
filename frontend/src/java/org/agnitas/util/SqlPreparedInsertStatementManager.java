/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SqlPreparedInsertStatementManager {
	private String baseStatement = null;
	private List<String> insertFieldNames = new ArrayList<>();
	private List<Object> insertValues = new ArrayList<>();
	private List<Boolean> specialSqlValueMarkers = new ArrayList<>();
	
	public SqlPreparedInsertStatementManager(String baseStatement) throws Exception {
		if (baseStatement == null || !baseStatement.toLowerCase().startsWith("insert into ")) {
			throw new Exception("Invalid baseStatement for insert statement");
		}
		
		this.baseStatement = baseStatement.trim();
	}
	
	/**
	 * Add a Value that will be used as a preparedStatement value (uses ?)
	 * @param fieldName
	 * @param value
	 */
	public void addValue(String fieldName, Object value) {
		addValue(fieldName, value, false);
	}
	
	/**
	 * Add a Value to the insert statement
	 * @param fieldName
	 * @param value
	 * @param isSpecialSqlValue
	 * 							if set that value will not be used as a preparedStatement value (no encapsulation by single quote, no use of ?)
	 * 							if NOT set that value will used as a normal preparedStatement value (uses ?)
	 */
	public void addValue(String fieldName, Object value, boolean isSpecialSqlValue) {
		insertFieldNames.add(fieldName);
		insertValues.add(value);
		specialSqlValueMarkers.add(isSpecialSqlValue);
	}
	
	public String getPreparedSqlString() {
		StringBuilder statement = new StringBuilder(baseStatement);
		statement.append(" (" + StringUtils.join(insertFieldNames, ", ") + ") VALUES (");
		
		for (int i = 0; i < specialSqlValueMarkers.size(); i++) {
			if (i > 0) {
				statement.append(", ");
			}
			
			if (insertValues.get(i) == null) {
				statement.append("null");
			} else if (specialSqlValueMarkers.get(i)) {
				statement.append(insertValues.get(i));
			} else {
				statement.append("?");
			}
		}

		statement.append(")");
		
		return statement.toString();
	}
	
	public Object[] getPreparedSqlParameters() {
		List<Object> preparedSqlParameters = new ArrayList<>();
		for (int i = 0; i < specialSqlValueMarkers.size(); i++) {
			if (!specialSqlValueMarkers.get(i) && insertValues.get(i) != null) {
				preparedSqlParameters.add(insertValues.get(i));
			}
		}

		if (preparedSqlParameters.size() > 0) {
			return preparedSqlParameters.toArray();
		} else {
			return null;
		}
	}
}
