/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import javax.sql.DataSource;

import org.agnitas.emm.extension.exceptions.DatabaseScriptException;
import org.agnitas.emm.extension.sqlparser.SimpleSqlParser;
import org.agnitas.emm.extension.sqlparser.validator.DatabaseScriptValidator;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseScriptExecutor {
	private static final transient Logger logger = Logger.getLogger( DatabaseScriptExecutor.class);
	
	private final DataSource dataSource;
	private final DatabaseScriptValidator validator;
	
	public DatabaseScriptExecutor( DataSource dataSource, DatabaseScriptValidator validator) {
		this.dataSource = dataSource;
		this.validator = validator;
	}
	
	public void execute( File file, String pluginId) throws DatabaseScriptException {
		if( logger.isInfoEnabled()) {
			logger.info( "Executing database script from file " + file.getAbsolutePath());
		}
		
		if( !file.exists()) {
			logger.info( "Database script " + file.getAbsolutePath() + " does not exist.");
			return;
		}

		try {
			try (FileInputStream fis = new FileInputStream(file)) {
				try (InputStreamReader fileReader = new InputStreamReader(fis, Charset.forName( "UTF-8"))) {
					SimpleSqlParser parser = new SimpleSqlParser( fileReader);
					List<String> statements = parser.parse();
					
					validator.validate(statements, namePrefixFromPluginId( pluginId));
					
					executeScript( statements);
				}
			}
		} catch( IOException e) {
			logger.error( "I/O error executing database script", e);
			
			throw new DatabaseScriptException( e);
		}
	}
	
	private String namePrefixFromPluginId( String pluginId) {
		return pluginId + "_";
	}
	
	private void executeScript( List<String> statements) throws DatabaseScriptException {
		if( logger.isInfoEnabled()) {
			logger.info( "Executing SQL script");
		}

		try {
			JdbcTemplate template = new JdbcTemplate( this.dataSource);
			
			for( String statement : statements) {
				if( statement.equals( ""))
					continue;
				
				if( logger.isDebugEnabled())
					logger.debug( "Executing statement: " + statement);
				
				template.execute( statement);
			}
		} catch( DataAccessException e) {
			logger.error( "Error executing database script", e);
			
			throw new DatabaseScriptException( e);
		}
	}
}
