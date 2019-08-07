/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.impl;

import javax.sql.DataSource;

import org.agnitas.emm.extension.PluginInstaller;
import org.agnitas.emm.extension.dao.PluginDao;
import org.agnitas.emm.extension.sqlparser.validator.DatabaseScriptValidator;
import org.agnitas.emm.extension.sqlparser.validator.impl.SimpleDatabaseScriptValidator;
import org.apache.log4j.Logger;

public class ExtensionSystemBuilder {
	
	private static final transient Logger logger = Logger.getLogger( ExtensionSystemBuilder.class);
	
	private String systemPluginBaseDirectory;
	private String pluginBaseDirectory;
	private String jspBaseDirectory;
	private String databaseName;
	
	private PluginDao pluginDao;
	private DataSource dataSource;
	
	public ExtensionSystemImpl createExtensionSystem() {
		if( logger.isInfoEnabled()) {
			logger.info( "Creating new extension system");
		}
		
		checkConfiguration();
		
		DatabaseScriptValidator scriptValidator = new SimpleDatabaseScriptValidator();
		ExtensionSystemConfiguration configuration = createExtensionSystemConfiguration();
		JspRestoreUtil jspRestoreUtil = new JspRestoreUtil( configuration);
		DatabaseScriptExecutor scriptExecutor = new DatabaseScriptExecutor( dataSource, scriptValidator);
		PluginInstaller pluginInstaller = createPluginInstaller( configuration, jspRestoreUtil, scriptExecutor);
		
		return createInstance( configuration, jspRestoreUtil, pluginInstaller, pluginDao);
	}
	
	public String getSystemPluginBaseDirectory() {
		return this.systemPluginBaseDirectory;
	}
	
	public String getPluginBaseDirectory() {
		return this.pluginBaseDirectory;
	}
	
	public String getJspBaseDirectory() {
		return this.jspBaseDirectory;
	}
	
	public String getDatabaseName() {
		return this.databaseName;
	}
	
	protected ExtensionSystemConfiguration createExtensionSystemConfiguration() {
		return new ExtensionSystemConfiguration( systemPluginBaseDirectory, pluginBaseDirectory, jspBaseDirectory, databaseName);
	}
	
	protected PluginInstaller createPluginInstaller( ExtensionSystemConfiguration configuration, JspRestoreUtil jspRestoreUtil, DatabaseScriptExecutor scriptExecutor) {
		return new PluginInstallerImpl( configuration, jspRestoreUtil, scriptExecutor);
	}
	
	protected ExtensionSystemImpl createInstance( ExtensionSystemConfiguration configuration, JspRestoreUtil jspRestoreUtil, PluginInstaller pluginInstaller, PluginDao pluginDao) {
		return new ExtensionSystemImpl( configuration, jspRestoreUtil, pluginInstaller, pluginDao);
	}
	
	public ExtensionSystemBuilder setSystemPluginBaseDirectory( String systemPluginBaseDirectory) {
		this.systemPluginBaseDirectory = systemPluginBaseDirectory;
		
		return this;
	}
	
	public ExtensionSystemBuilder setPluginBaseDirectory( String pluginBaseDirectory) {
		this.pluginBaseDirectory = pluginBaseDirectory;
		
		return this;
	}
	
	public ExtensionSystemBuilder setJspBaseDirectory( String jspBaseDirectory) {
		this.jspBaseDirectory = jspBaseDirectory;
		
		return this;
	}

	public ExtensionSystemBuilder setPluginDao( PluginDao pluginDao) {
		this.pluginDao = pluginDao;
		
		return this;
	}
	
	public ExtensionSystemBuilder setDataSource( DataSource dataSource) {
		this.dataSource = dataSource;
		
		return this;
	}
	
	public ExtensionSystemBuilder setDatabaseName( String databaseName) {
		this.databaseName = databaseName;
		
		return this;
	}
	
	protected void checkConfiguration() {
		if( logger.isInfoEnabled()) {
			logger.info( "Checking configuration for extension system");
		}
		
		if( systemPluginBaseDirectory == null) {
			logger.error( "Directory for system-plugins not set");
			throw new IllegalStateException( "system-plugin directory not set");
		}
		
		if( pluginBaseDirectory == null) {
			logger.error( "Directory for additional plugins not set");
			throw new IllegalStateException( "plugin directory not set");
		}
		
		if( jspBaseDirectory == null) {
			logger.error( "Working directory for JSPs not set");
			throw new IllegalStateException( "JSP base directory for plugins not set");
		}
		
		if( pluginDao == null) {
			logger.error( "Plugin DAO not set");
			throw new IllegalStateException( "Plugin DAO not set");
		}
		
		if( dataSource == null) {
			logger.error( "Data source not set");
			throw new IllegalStateException( "Data source not set");
		}
		
		if( databaseName == null) {
			logger.error( "Database name is not set");
			throw new IllegalStateException( "Database name not set");
		}

		if( logger.isInfoEnabled()) {
			logger.info( "Configuration is good");
		}
	}
}
