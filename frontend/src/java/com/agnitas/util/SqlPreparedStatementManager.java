/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SqlPreparedStatementManager {

	private static final String SQL_WHERE_SIGN = "WHERE";
	private static final String SQL_OPERATOR_AND = "AND";
	private static final String SQL_OPERATOR_OR = "OR";
	
	private final StringBuilder statement = new StringBuilder();
	private final List<Object> statementParameters = new ArrayList<>();
	private boolean hasAppendedClauses = false;
	
	public SqlPreparedStatementManager(String baseStatement) {
		String baseStatementString = baseStatement.trim();
		if (baseStatementString.toLowerCase().endsWith(" " + SQL_WHERE_SIGN.toLowerCase())) {
			statement.append(baseStatementString.substring(0, baseStatementString.length() - SQL_WHERE_SIGN.length() - 1).trim());
		} else {
			statement.append(baseStatementString.trim());
		}
	}

	public SqlPreparedStatementManager(String baseStatement, Object... parameter) {
		String baseStatementString = baseStatement.trim();
		if (baseStatementString.toLowerCase().endsWith(" " + SQL_WHERE_SIGN.toLowerCase())) {
			statement.append(baseStatementString.substring(0, baseStatementString.length() - SQL_WHERE_SIGN.length() - 1).trim());
		} else {
			statement.append(baseStatementString.trim());
		}

        if (parameter != null) {
			statementParameters.addAll(Arrays.asList(parameter));
        }
	}

	/**
	 * Append a where clause to the statement concatenated by " AND "
	 */
	public void addWhereClause(String whereClause, Object... parameter) {
		addWhereClause(false, whereClause, parameter);
	}
	
	/**
	 * Append a where clause to the statement.
	 * 
	 * @param concatenateByOr type of concatenation (false = AND / true = OR)
	 */
	public void addWhereClause(boolean concatenateByOr, String whereClause, Object... parameter) {
		if (StringUtils.isBlank(whereClause)) {
			throw new IllegalArgumentException("Invalid empty where clause");
		}
		
		int numberOfParameterPlaceholders = 0;
		boolean withinStringQuotes = false;
		for (char currentChar : whereClause.toCharArray()) {
			if ('\'' == currentChar) {
				withinStringQuotes = !withinStringQuotes;
			} else if ('?' == currentChar && !withinStringQuotes) {
				numberOfParameterPlaceholders++;
			}
		}
		
		int parametersLength = parameter == null ? 0 : parameter.length;
		if (parametersLength != numberOfParameterPlaceholders) {
			throw new IllegalArgumentException(String.format("Invalid number of parameters in where clause (%s, got %d params)", whereClause, parametersLength));
		}
		
		if (hasAppendedClauses) {
			statement.append(" ");
			if (concatenateByOr){
				addOrClause();
			} else {
				addAndClause();
			}
		} else {
			statement.append(" ");
			statement.append(SQL_WHERE_SIGN);
		}
		
		addWhereClauseSimple(whereClause, parameter);
			
		hasAppendedClauses = true;
	}

	public void addOrClause(){
		statement.append(" ").append(SQL_OPERATOR_OR).append(" ");
	}

	public void addAndClause(){
		statement.append(" ").append(SQL_OPERATOR_AND).append(" ");
	}

	public void addWhereClauseSimple(String whereClause, Object... parameter){
		statement.append(" (");
		statement.append(whereClause.trim());
		statement.append(")");
		if (parameter != null) {
			Collections.addAll(statementParameters, parameter);
		}
	}

	public void appendOpeningParenthesis(){
		if(hasAppendedClauses){
			statement.append(" ( ");
		}
	}
	public void appendClosingParenthesis(){
		if(hasAppendedClauses){
			statement.append(" ) ");
		}
	}

    /**
     * Append a limit clause to the statement.
     */
    public void appendLimitClause(String limitClause, Object... parameter) {
        if (StringUtils.isBlank(limitClause)) {
            throw new IllegalArgumentException("Invalid empty where clause");
        }

        int numberOfQuestionMarks = StringUtils.countMatches(limitClause, "?");
        if ((parameter == null && numberOfQuestionMarks != 0) || (parameter != null && numberOfQuestionMarks != parameter.length)) {
            throw new IllegalArgumentException("Invalid number of parameters in where clause");
        }

        statement.append(" ");
        statement.append(limitClause.trim());

        if (parameter != null) {
			statementParameters.addAll(Arrays.asList(parameter));
        }
    }

	public void finalizeStatement(String statementTail) {
		statement.append(" ");
		statement.append(statementTail.trim());
	}

	public void setHasAppendedClauses(boolean hasAppendedClauses) {
		this.hasAppendedClauses = hasAppendedClauses;
	}

	public String getPreparedSqlString() {
		return statement.toString();
	}
	
	public void appendToPreparedSqlString(String append) {
		statement.append(append);
	}
	
	public Object[] getPreparedSqlParameters() {
		if (statementParameters.size() > 0) {
			return statementParameters.toArray();
		} else {
			return null;
		}
	}
}
