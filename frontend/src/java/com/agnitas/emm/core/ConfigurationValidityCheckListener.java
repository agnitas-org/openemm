/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ConfigurationValidityCheckListener implements ServletContextListener {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ConfigurationValidityCheckListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    	try {
			WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContextEvent.getServletContext());
			ConfigurationValidityCheck configurationValidityCheck = webApplicationContext.getBean("ConfigurationValidityCheck", ConfigurationValidityCheck.class);
			configurationValidityCheck.checkValidity(webApplicationContext);
		} catch (Exception e) {
			logger.error("Cannot check installation validity: " + e.getMessage(), e);
		}
    }

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
        // Nothing to be done here
	}
}
