/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.sqlparser.validator;

import java.util.List;

import org.agnitas.emm.extension.exceptions.DatabaseScriptException;

/**
 * Interface for implementations to validate SQL scripts.
 */
public interface DatabaseScriptValidator {
	
	/**
	 * Validate the list of SQL statements.
	 * 
	 * @param statements list of SQL statements
	 * @param namePrefix name prefix used for validation of identifiers (table names, ...)
	 * 
	 * @throws DatabaseScriptException on errors validating the statements (invalid names, ...)
	 */
	public void validate( List<String> statements, String namePrefix) throws DatabaseScriptException;
}
