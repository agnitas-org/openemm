/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.extension.impl;

import org.agnitas.emm.extension.impl.ExtensionSystemConfiguration;
import org.apache.log4j.Logger;

public class ComExtensionSystemConfiguration extends ExtensionSystemConfiguration {

	private static final transient Logger logger = Logger.getLogger( ComExtensionSystemConfiguration.class);
	
	private final String birtHost;
	private final String birtPluginBaseDirectory;
	private final String birtHostUser;
	
	public ComExtensionSystemConfiguration( String systemPluginBaseDirectory, String pluginBaseDirectory, String jspBaseDirectory, String databaseName, String birtHost, String birtHostUser, String birtPluginBaseDirectory) {
		super( systemPluginBaseDirectory, pluginBaseDirectory, jspBaseDirectory, databaseName);
		
		this.birtHost = birtHost;
		this.birtPluginBaseDirectory = birtPluginBaseDirectory;
		this.birtHostUser = birtHostUser;

		if( logger.isDebugEnabled()) {
			logger.debug( "BIRT host: " + this.birtHost);
			logger.debug( "BIRT host user: " + this.birtHostUser);
			logger.debug( "BIRT plugin base directory: " + this.birtPluginBaseDirectory);
		}
	}
	
	public String getBirtHost() {
		return this.birtHost;
	}
	
	public String getBirtHostUser() {
		return this.birtHostUser;
	}
	
	public String getBirtPluginBaseDirectory() {
		return this.birtPluginBaseDirectory;
	}

}
