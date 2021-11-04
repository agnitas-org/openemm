/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This context listener instantiates the backend datsource
 */
public class BackendDatasourceInitializationContextListener implements ServletContextListener {
	private static final transient Logger logger = Logger.getLogger(BackendDatasourceInitializationContextListener.class);

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			ServletContext servletContext = event.getServletContext();
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			DBase.DATASOURCE = (DataSource) applicationContext.getBean("dataSource");
			if (DBase.DATASOURCE == null) {
				logger.error("Datasource in DBase for Backend was empty. Backend will try to create its own datasource");
			}
		} catch (Exception e) {
			logger.error("Cannot set Datasource in DBase for Backend. Backend will try to create its own datasource", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing to do
	}
}
