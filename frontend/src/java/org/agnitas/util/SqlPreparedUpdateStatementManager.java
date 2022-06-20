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

public class SqlPreparedUpdateStatementManager {
	private static final String SQL_SET_SIGN = "SET";
	private static final String SQL_WHERE_SIGN = "WHERE";
	private static final String SQL_OPERATOR_AND = "AND";
	private static final String SQL_OPERATOR_OR = "OR";
	
	private String baseStatement = null;
	private List<String> updateFieldNames = new ArrayList<>();
	private List<Object> updateValues = new ArrayList<>();
	private List<Boolean> specialSqlValueMarkers = new ArrayList<>();
	private StringBuilder whereClauseBuilder = new StringBuilder("");
	private List<Object> whereClauseValues = new ArrayList<>();
	
	public SqlPreparedUpdateStatementManager(String baseStatement) throws Exception {
		if (baseStatement == null || !baseStatement.toLowerCase().startsWith("update ")) {
			throw new Exception("Invalid baseStatement for update statement");
		}
		
		baseStatement = baseStatement.trim();
		
		if (baseStatement.toLowerCase().endsWith(" " + SQL_SET_SIGN.toLowerCase())) {
			baseStatement = baseStatement.substring(0, baseStatement.length() - SQL_SET_SIGN.length() - 1).trim();
		}
		
		this.baseStatement = baseStatement + " " + SQL_SET_SIGN + " ";
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
		updateFieldNames.add(fieldName);
		updateValues.add(value);
		specialSqlValueMarkers.add(isSpecialSqlValue);
	}
	
	/**
	 * Append a where clause to the statement concatenated by " AND "
	 * 
	 * @param whereClause
	 * @param parameter
	 * @throws Exception 
	 */
	public void addWhereClause(String whereClause, Object... parameter) throws Exception {
		addWhereClause(false, whereClause, parameter);
	}
	
	/**
	 * Append a where clause to the statement.
	 * 
	 * @param concatenateByOr type of concatenation (false = AND / true = OR)
	 * @param whereClause
	 * @param parameter
	 * @throws Exception 
	 */
	public void addWhereClause(boolean concatenateByOr, String whereClause, Object... parameter) throws Exception {
		if (StringUtils.isBlank(whereClause)) {
			throw new Exception("Invalid empty where clause");
		}
		
		int numberOfQuestionMarks = StringUtils.countMatches(whereClause, "?");
		if ((parameter == null && numberOfQuestionMarks != 0) || (parameter != null && numberOfQuestionMarks != parameter.length)) {
			throw new Exception("Invalid number of parameters in where clause");
		}
		
		if (whereClauseBuilder.length() > 0) {
			whereClauseBuilder.append(" ");
			whereClauseBuilder.append(concatenateByOr ? SQL_OPERATOR_OR : SQL_OPERATOR_AND);
		} else {
			whereClauseBuilder.append(" ");
			whereClauseBuilder.append(SQL_WHERE_SIGN);
		}
		
		whereClauseBuilder.append(" (");
		whereClauseBuilder.append(whereClause.trim());
		whereClauseBuilder.append(")");
		if (parameter != null) {
			for (Object item : parameter) {
				whereClauseValues.add(item);
			}
		}
	}
	
	public String getPreparedSqlString() {
		StringBuilder statement = new StringBuilder(baseStatement);
		
		for (int i = 0; i < specialSqlValueMarkers.size(); i++) {
			if (i > 0) {
				statement.append(", ");
			}

			statement.append(updateFieldNames.get(i));
			statement.append(" = ");
			
			if (updateValues.get(i) == null) {
				statement.append("null");
			} else if (specialSqlValueMarkers.get(i)) {
				statement.append(updateValues.get(i));
			} else {
				statement.append("?");
			}
		}
		
		statement.append(whereClauseBuilder.toString());
		
		return statement.toString();
	}
	
	public Object[] getPreparedSqlParameters() {
		List<Object> preparedSqlParameters = new ArrayList<>();
		for (int i = 0; i < specialSqlValueMarkers.size(); i++) {
			if (!specialSqlValueMarkers.get(i) && updateValues.get(i) != null) {
				preparedSqlParameters.add(updateValues.get(i));
			}
		}

		preparedSqlParameters.addAll(whereClauseValues);
		
		if (preparedSqlParameters.size() > 0) {
			return preparedSqlParameters.toArray();
		} else {
			return null;
		}
	}
}
