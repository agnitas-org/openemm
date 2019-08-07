/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.extensions;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;

import com.agnitas.emm.extension.impl.ComExtensionConstants;

/**
 * Implementation of {@link ServletContextListener} for initialization of extension system.
 */
public class PluginInstallationServletContextListener implements ServletContextListener {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( PluginInstallationServletContextListener.class);

	@Override
	public void contextDestroyed( ServletContextEvent event) {
		// Nothing to do when destroying the context
	}

	@Override
	public void contextInitialized( ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();
		
		String reportDesignPath = servletContext.getRealPath( ComExtensionConstants.PLUGIN_BIRT_RPTDESIGN_BASE);
		String scriptlibPath = servletContext.getRealPath(ComExtensionConstants.PLUGIN_BIRT_SCRIPTLIB_BASE);
		String pluginsPath;
		try {
			pluginsPath = ConfigService.getInstance().getValue(ConfigValue.EmmPluginsHome);
		} catch (Exception e) {
			logger.error("Cannot read EmmPluginsHome: " + e.getMessage(), e);
			throw new RuntimeException("Cannot read EmmPluginsHome: " + e.getMessage(), e);
		}
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Path for report designs: " + reportDesignPath);
			logger.debug( "Path for script libs: " + scriptlibPath);
			logger.debug( "Path for plugin ZIPs: " + pluginsPath);
		}
		
		BirtPluginInstaller installer = new BirtPluginInstaller( pluginsPath, reportDesignPath, scriptlibPath);
		installer.installPlugins();
	}
}
