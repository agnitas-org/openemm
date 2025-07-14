/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.sql;

import com.agnitas.emm.core.target.eql.codegen.CodeFragment;
import com.agnitas.emm.core.target.eql.codegen.EqlDateFormat;

/**
 * Common interface used by {@link DefaultCommonSqlCodeGeneratorCallback} for generation of
 * code depending on the SQL dialect used by the database.
 */
public interface SqlDialect {

	/**
	 * Generate code for string concatenation.
	 * 
	 * @param left first (or left) expression
	 * @param right second (or right) expression
	 * 
	 * @return code for string concatenation
	 */
	String stringConcat(String left, String right);

	/**
	 * Generated the code for checking if given operand is empty (NULL or '').
	 * 
	 * @param code code of operand
	 * 
	 * @return code for check
	 */
	String isEmpty(CodeFragment code);

	/**
	 * Returns the SQL keyword for current date.
	 * 
	 * @return SQL keyword for current date
	 */
	String today();

	/**
	 * Returns the SQL expression for conversion a date value to a string.
	 * 
	 * @param code date expression to convert to string
	 * @param dateFormat date format
	 * 
	 * @return SQL expression for date-to-string-conversion
	 */
	String dateToString(String code, EqlDateFormat dateFormat);

	/**
	 * Converts the given EQL date format to database specific date format.
	 * 
	 * @param dateFormat EQL date format
	 * 
	 * @return SQL specific date format
	 */
	String dateFormat(EqlDateFormat dateFormat);

	/**
	 * Returns the date arithmetics expression for adding an amount of days to
	 * a given date expression.
	 *  
	 * @param left date expression
	 * @param right amount of days
	 * 
	 * @return SQL specific expression for date arithmetics
	 */
	String dateAddDays(String left, String right);

	/**
	 * Returns the date arithmetics expression for subtracting an amount of days to
	 * a given date expression.
	 *  
	 * @param left date expression
	 * @param right amount of days
	 * 
	 * @return SQL specific expression for date arithmetics
	 */
	String dateSubDays(String left, String right);

}
