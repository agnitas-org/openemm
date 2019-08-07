/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.extension.dao.PluginDao;
import org.agnitas.emm.extension.impl.ExtensionSystemBuilder;
import org.agnitas.emm.extension.impl.ExtensionSystemImpl;
import org.agnitas.emm.extension.util.ExtensionConstants;
import org.agnitas.util.DbUtilities;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This context listener instantiates the extension system for
 * a single web application.
 */
public class ExtensionSystemInitializationContextListener implements ServletContextListener {

	private static final transient Logger logger = Logger.getLogger(ExtensionSystemInitializationContextListener.class);
	
	protected ConfigService configService;

	/**
	 * Cache variable for the dataSource vendor, so it must not be recalculated everytime.
	 * This variable may be uninitialized before the first execution of the isOracleDB method
	 */
	private static Boolean IS_ORACLE_DB = null;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		configService = webApplicationContext.getBean("ConfigService", ConfigService.class);
		
		
		if (servletContext.getAttribute(ExtensionConstants.EXTENSION_SYSTEM_APPLICATION_SCOPE_ATTRIBUTE) != null) {
			logger.fatal ("Extension system already initialized for the application context");
		} else {
			try {
				ExtensionSystemImpl extensionSystem = createExtensionSystem( servletContext);
				servletContext.setAttribute(ExtensionConstants.EXTENSION_SYSTEM_APPLICATION_SCOPE_ATTRIBUTE, extensionSystem);
				
				extensionSystem.startUp();
			} catch (Exception e) {
				logger.fatal("Error initializing extension system", e);				
			}
		}
	}

	protected ExtensionSystemImpl createExtensionSystem(ServletContext servletContext) throws Exception {
		WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		
		ExtensionSystemBuilder extensionSystemBuilder = createExtensionSystemBuilder();
		setBuilderProperties(extensionSystemBuilder, servletContext, springContext);

		return extensionSystemBuilder.createExtensionSystem();
	}

	protected void setBuilderProperties(ExtensionSystemBuilder extensionSystemBuilder, ServletContext servletContext, WebApplicationContext springContext) throws Exception {
		DataSource dataSource = springContext.getBean("dataSource", DataSource.class);

		// Directories
		extensionSystemBuilder.setJspBaseDirectory(servletContext.getRealPath("/plugins"));
		extensionSystemBuilder.setPluginBaseDirectory(configService.getValue(ConfigValue.EmmPluginsHome));
		extensionSystemBuilder.setSystemPluginBaseDirectory(servletContext.getRealPath("/WEB-INF/system-plugins"));

		// Misc. values
		String databaseName;
		if (isOracleDB(dataSource)) {
			databaseName = "oracle";
		} else {
			databaseName = "mysql";
		}
		extensionSystemBuilder.setDatabaseName(databaseName);

		// Used components
		extensionSystemBuilder.setDataSource(dataSource);
		extensionSystemBuilder.setPluginDao(springContext.getBean("pluginDao", PluginDao.class));
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();
		ExtensionSystemImpl extensionSystem = (ExtensionSystemImpl) servletContext.getAttribute(ExtensionConstants.EXTENSION_SYSTEM_APPLICATION_SCOPE_ATTRIBUTE);
		
		if (extensionSystem != null) {
			logger.info( "Shutting down ExtensionSystem");
			extensionSystem.shutdown();
			logger.info( "ExtensionSystem is shut down");
		}
	}
	
	protected ExtensionSystemBuilder createExtensionSystemBuilder() {
		return new ExtensionSystemBuilder();
	}
	
	/**
	 * Checks the db vendor of the dataSource and caches the result for further usage
	 * @return true if db vendor of dataSource is Oracle, false if any other vendor (e.g. mysql)
	 */
	protected final boolean isOracleDB(DataSource dataSource) {
		if (IS_ORACLE_DB == null) {
			IS_ORACLE_DB = DbUtilities.checkDbVendorIsOracle(dataSource);
		}
		return IS_ORACLE_DB;
	}
}
