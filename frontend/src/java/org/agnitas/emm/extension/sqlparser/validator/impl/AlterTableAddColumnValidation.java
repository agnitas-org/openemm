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
 * Implementation of the StatementValidation interface providing a simple validation of 
 * <code>ALTER TABLE ... ADD</code>.
 * 
 *  This validation allowes only one single modification per <i>ALTER TABLE</i>, in this case
 *  adding a single column or multiple columns.
 *
 * @see StatementValidation
 */
class AlterTableAddColumnValidation extends BasicValidation {

	/** Logger. */
	private static final transient Logger logger = Logger.getLogger( AlterTableAddColumnValidation.class);
	
	/** Pattern for statement recognition. */
	private final Pattern ALTER_TABLE_PATTERN = Pattern.compile( "^\\s*alter\\s+table\\s+([^ ]+)\\s+add\\s+(?:column\\s+)?(.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
	
	/** Pattern for removing leading and trailing spaces. */
	private final Pattern COLUMN_PATTERN = Pattern.compile( "^\\s*(.*?)\\s.*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);

	@Override
	public boolean validate(String statement, String namePrefix) throws DatabaseScriptException {
		Matcher matcher = ALTER_TABLE_PATTERN.matcher( statement);
		
		if( !matcher.matches()) {
			if( logger.isInfoEnabled())
				logger.info( "Statement does not match regular expression");
			
			return false;
		}
		
		String tableName = matcher.group( 1);
		String columnDefinitions = matcher.group( 2);
		
		if( logger.isDebugEnabled()) {
			logger.debug( "table name: " + tableName);
			logger.debug( "column definiton: " + columnDefinitions);
		}

		if( tableNameHasPluginPrefix( tableName, namePrefix)) {
			validateTableName( tableName, namePrefix);
			validateColumnDefinitions( columnDefinitions, namePrefix, false);
		} else {
			validateColumnDefinitions( columnDefinitions, namePrefix, true);
		}
			
		return true;
	}
	
	/**
	 * Check the column definitions.
	 * 
	 * @param columnDefinitions column definitions
	 * @param prefix name prefix for column names
	 * @param validateNames check column names when set to true.
	 * 
	 * @throws DatabaseScriptException on validation errors
	 */
	private void validateColumnDefinitions( String columnDefinitions, String prefix, boolean validateNames) throws DatabaseScriptException {
		// Column definitions may be in parenthesis
		
		String fixedColumnDefinitions = fixColumnDefinitions( columnDefinitions);		
		String[] columnDefinitionArray = fixedColumnDefinitions.split( ",");
		
		for( String columnDefinition : columnDefinitionArray)
			validateColumnDefinition( columnDefinition, prefix, validateNames);
	}
	
	/**
	 * Validate a single column definition.
	 * 
	 * @param columnDefinition column definition
	 * @param prefix name prefix for column names
	 * @param validateName validate column name when set to true
	 * 
	 * @throws DatabaseScriptException on errors validating the column definition
	 */
	private void validateColumnDefinition( String columnDefinition, String prefix, boolean validateName) throws DatabaseScriptException {
		if( logger.isInfoEnabled())
			logger.info( "Validating column definition: " + columnDefinition);
		
		Matcher matcher = COLUMN_PATTERN.matcher( columnDefinition);
		
		if( !matcher.matches())
			throw new DatabaseScriptException( "Error in column definition? (" + columnDefinition + ")");
		
		String columnName = matcher.group( 1);
		
		if( logger.isDebugEnabled())
			logger.debug( "Column name: " + columnName);
		
		if( validateName) {
			validateColumnName( columnName, prefix);
		}
	}

	private String fixColumnDefinitions( String columnDefinitions) {
		String fixedColumnDefinitions = columnDefinitions.trim();
		
		if( columnDefinitions.startsWith("(")) {
			fixedColumnDefinitions = fixedColumnDefinitions.substring( 1, fixedColumnDefinitions.lastIndexOf( ')'));
			
			if( logger.isInfoEnabled())
				logger.info( "Corrected column definition due to parenthesis: " + fixedColumnDefinitions);
		}
		
		return fixedColumnDefinitions;
	}
}
