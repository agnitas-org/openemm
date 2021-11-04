/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;

import com.agnitas.messages.DBMessagesResource;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.dataset.BIRTDataSet;

public class BirtMessagesPropertiesContextListener implements ServletContextListener {

	private static final transient Logger logger = Logger.getLogger(BirtMessagesPropertiesContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
        	// Birt must generate messages in here, because it has no struts like EMM
        	// Messages are then available via I18nString
        	DBMessagesResource dbMessagesResource = new DBMessagesResource();
    		dbMessagesResource.init();
    		
    		ServletContext servletContext = event.getServletContext();
    		if (ConfigService.getInstance().getBooleanValue(ConfigValue.IsLiveInstance)) {
    			createMessagesPropertiesFiles(servletContext);
    		}
			
			new BIRTDataSet().resetTempDatabase();
		} catch (Exception e) {
			logger.error("MessagesPropertiesGeneratorContextListener init: " + e.getMessage(), e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do
	}

	private void createMessagesPropertiesFiles(ServletContext servletContext) throws Exception {
		try {
			logger.info("Creating MessagesPropertiesFiles");

			// TODO Use WebAppFileUtil.getWebInfDirectoryPath()
			String path = servletContext.getRealPath("/WEB-INF/classes");
			
			Map<String, Map<String, String>> allMessages = I18nString.MESSAGE_RESOURCES.getAllMessages();

			for (Entry<String, Map<String, String>> allMessagesEntry : allMessages.entrySet()) {
					Properties properties = new Properties();
				Map<String, String> messages = allMessagesEntry.getValue();
				for (Entry<String, String> messagesEntry : messages.entrySet()) {
					if (messagesEntry.getValue() != null) {
						String message = messagesEntry.getValue().replace("\\n", "\n");
						properties.setProperty(messagesEntry.getKey(), message);
					}
				}

				String fileName = allMessagesEntry.getKey().equals("default") ? "messages.properties" : "messages_" + allMessagesEntry.getKey() + ".properties";
				try (FileOutputStream fileOutputStream = new FileOutputStream(new File(path, fileName))) {
					properties.store(fileOutputStream, "This file is auto-generated");
				}
			}

			logger.info("MessagesPropertiesFiles created");
		} catch (IOException e) {
			logger.error("Can't generate MessagesPropertiesFiles", e);
		}
	}
}
