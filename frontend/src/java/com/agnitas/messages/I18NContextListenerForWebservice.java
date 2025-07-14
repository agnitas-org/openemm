/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.messages;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class I18NContextListenerForWebservice implements ServletContextListener {
	private static final transient Logger logger = LogManager.getLogger(I18NContextListenerForWebservice.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
        	if (I18nString.MESSAGE_RESOURCES == null) {
    			DBMessagesResource dbMessagesResource = new DBMessagesResource();
    			dbMessagesResource.init();
    		}
		} catch (Exception e) {
			logger.error("I18NContextListenerForWebservice init: " + e.getMessage(), e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// Do nothing
	}
}
