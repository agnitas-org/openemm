/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.sqlparser.validator.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.emm.extension.exceptions.DatabaseScriptException;
import org.apache.log4j.Logger;

/**
 * Implementation of the StatementValidation interface providing a simple subset of the <code>ALTER TABLE ... DROP ...</code> statement.
 * 
 * This implementation allows only a single modification. In this case, dropping columns.
 * There is only one column per <i>ALTER TABLE</i> allowed.
 *
 * @see StatementValidation
 */
class AlterTableDropColumnValidation extends BasicValidation {
	
	/** Logger. */
	private static final transient Logger logger = Logger.getLogger( AlterTableDropColumnValidation.class);
	
	/** Pattern unsed for statement recognition. */
	private final Pattern ALTER_TABLE_PATTERN = Pattern.compile( "^\\s*alter\\s+table\\s+([^ ]+)\\s+drop\\s+(?:column\\s+)?([^ ,]+)\\s*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);

	@Override
	public boolean validate(String statement, String namePrefix) throws DatabaseScriptException {
		Matcher matcher = ALTER_TABLE_PATTERN.matcher( statement);
		
		if( !matcher.matches()) {
			if( logger.isInfoEnabled())
				logger.info( "Statement does not match regular expression");
			
			return false;
		}
		
		String tableName = matcher.group( 1);
		String columnName = matcher.group( 2);
		
		if( logger.isDebugEnabled()) {
			logger.debug( "table name: " + tableName);
			logger.debug( "column name: " + columnName);
		}

		if( tableNameHasPluginPrefix( tableName, namePrefix)) {
			validateTableName( tableName, namePrefix);
		} else {
			validateColumnName( columnName, namePrefix);
		}
		
		return true;
	}
}
