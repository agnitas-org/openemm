/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.sqlparser.validator.impl;

import org.agnitas.emm.extension.exceptions.DatabaseScriptException;

/**
 * Interface used to implement validations for different SQL statements.
 */
interface StatementValidation {
	
	/**
	 * Attempts to validate the given statement. The name prefix can be used to check, if identifier (table names, column names, and so on) 
	 * matches a required format.
	 * 
	 * Three different results are possible:
	 * <ol>
	 * 	<li>The validation step is not responsible for the given statement. In this case, the methods returns <i>false</i></li>
	 *  <li>The validation step is responsible for the given statement and the statement is valid. The method returns <i>true</i></li>
	 *  <li>The validation step is responsible for the given statement but the validation fails. The method throws a DatabaseScriptException.</li>
	 * </ol>	
	 *  
	 * @param statement SQL statement to validate
	 * @param namePrefix name prefix used to check identifiers
	 * 
	 * @return false, when the validation step is not responsible for the given statement or true, when it is responsible for the given statement
	 * and the validation was successful. 
	 * 
	 * @throws DatabaseScriptException when the validation step is responsible for the given statement but the validation of the statement failed.
	 */
	public boolean validate( String statement, String namePrefix) throws DatabaseScriptException;
}
