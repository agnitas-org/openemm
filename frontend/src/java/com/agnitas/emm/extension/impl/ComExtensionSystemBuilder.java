/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.extension.impl;

import org.agnitas.emm.extension.PluginInstaller;
import org.agnitas.emm.extension.dao.PluginDao;
import org.agnitas.emm.extension.impl.DatabaseScriptExecutor;
import org.agnitas.emm.extension.impl.ExtensionSystemBuilder;
import org.agnitas.emm.extension.impl.ExtensionSystemConfiguration;
import org.agnitas.emm.extension.impl.ExtensionSystemImpl;
import org.agnitas.emm.extension.impl.JspRestoreUtil;
import org.apache.log4j.Logger;

public class ComExtensionSystemBuilder extends ExtensionSystemBuilder {
	
	private static final transient Logger logger = Logger.getLogger( ComExtensionSystemBuilder.class);
	
	private String birtHost;
	private String birtHostUser;
	private String birtPluginDirectory;
	
	public ExtensionSystemBuilder setBirtHost( String birtHost) {
		this.birtHost = birtHost;
		
		return this;
	}
	
	public ExtensionSystemBuilder setBirtHostUser( String birtHostUser) {
		this.birtHostUser = birtHostUser;
		
		return this;
	}
	
	public ExtensionSystemBuilder setBirtPluginDirectory( String birtPluginDirectory) {
		this.birtPluginDirectory = birtPluginDirectory;
		
		return this;
	}
	
	@Override
	protected ExtensionSystemImpl createInstance( ExtensionSystemConfiguration configuration, JspRestoreUtil jspRestoreUtil, PluginInstaller pluginInstaller, PluginDao pluginDao) {
		return new ComExtensionSystemImpl(configuration, jspRestoreUtil, pluginInstaller, pluginDao);
	}

	@Override
	protected PluginInstaller createPluginInstaller( ExtensionSystemConfiguration configuration, JspRestoreUtil jspRestoreUtil, DatabaseScriptExecutor scriptExecutor) {
		return new ComPluginInstallerImpl( configuration, jspRestoreUtil, scriptExecutor);
	}
	
	@Override
	protected ExtensionSystemConfiguration createExtensionSystemConfiguration() {
		return new ComExtensionSystemConfiguration( 
				getSystemPluginBaseDirectory(), 
				getPluginBaseDirectory(), 
				getJspBaseDirectory(), 
				getDatabaseName(),
				birtHost,
				birtHostUser,
				birtPluginDirectory);
	}


	@Override
	protected void checkConfiguration() {
		
		if( birtHost == null) {
			logger.error( "No BIRT host set");
			throw new IllegalStateException( "BIRT host not set");
		}
		
		if( birtHostUser == null) {
			logger.error( "No BIRT host user set");
			throw new IllegalStateException( "BIRT host user not set");
		}
		
		if( birtPluginDirectory == null) {
			logger.error( "No BIRT plugin directory set");
			throw new IllegalStateException( "BIRT plugin directory not set");
		}

		super.checkConfiguration();
	}
}
