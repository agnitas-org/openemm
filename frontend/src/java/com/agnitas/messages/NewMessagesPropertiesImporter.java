/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.messages;

import java.io.InputStream;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.dao.ComMessageDao;

/**
 * Importer for new_messages.properties file into EMM database messages_tbl
 * 
 * There are 3 kinds of entries allowed in this file:
 * - "DELETE.keynamex.keynamey"
 * 		Fully deletes the key "keynamex.keynamey" from messages_tbl
 * - "langKey.DELETE.keynamex.keynamey"
 * 		Deletes the value in the given language for the key "keynamex.keynamey" in messages_tbl
 * 		Deleting the value for the default language means to fully delete the key "keynamex.keynamey" from messages_tbl 
 * - "langKey.keynamex.keynamey"
 * 		Inserts the value in the given language for the key "keynamex.keynamey" in messages_tbl
 * 
 * For Updates of existing entries of key you need to delete an entry value (completely or just a language value)
 * and recreate the entry (completely or just a language value)
 */
public class NewMessagesPropertiesImporter {
	private static final transient Logger logger = LogManager.getLogger(NewMessagesPropertiesImporter.class);
	
	private static final String NEWMESSAGES_FILENAME = "new_messages.properties";
	
    private static final String DELETE_KEY_SIGN = "DELETE";

	public static void importNewMessagesProperties(ComMessageDao messageDao) {
        OrderedProperties newMessagesProperties = new OrderedProperties();
        try (InputStream newMessagesPropertiesInputStream = NewMessagesPropertiesImporter.class.getClassLoader().getResourceAsStream(NEWMESSAGES_FILENAME)) {
            if (newMessagesPropertiesInputStream == null) {
            	logger.warn("File " + NEWMESSAGES_FILENAME + " is missing and no messages were inserted, updated or deleted. Basepath: " + NewMessagesPropertiesImporter.class.getClassLoader().getResource("."));
            } else {
	            newMessagesProperties.load(newMessagesPropertiesInputStream);
	
	            Enumeration<Object> propertiesKeys = newMessagesProperties.keys();
	            while (propertiesKeys.hasMoreElements()) {
	                String propertyName = (String) propertiesKeys.nextElement();
	                String[] propertyNameParts = propertyName.split("\\.");
	
	                if (propertyNameParts.length < 2) {
	                	throw new Exception("Invalid property name found: " + propertyName);
	                }
	                
	                // Execute operations
	                if (DELETE_KEY_SIGN.equals(propertyNameParts[0])) {
	                	// Execute delete operation
	                    String propertyKey = propertyName.substring(DELETE_KEY_SIGN.length() + 1);
	                    messageDao.markAsDeleted(propertyKey, null);
	                } else {
	                	String language = propertyNameParts[0];
	                    if (language.length() != 2 || !language.toLowerCase().equals(language)) {
	                    	throw new Exception("Invalid language in property name found: " + propertyName);
	                    }
	                    
	                    if (DELETE_KEY_SIGN.equals(propertyNameParts[1])) {
                    		// Execute global delete operation
                            String propertyKey = propertyName.substring(language.length() + DELETE_KEY_SIGN.length() + 2);
                            messageDao.markAsDeleted(propertyKey, language);
	                    } else {
                            String propertyValue = newMessagesProperties.getProperty(propertyName);
                        	// Execute single language value insert operation
                            String propertyKey = propertyName.substring(language.length() + 1);
                            messageDao.insertMessage(propertyKey, language, propertyValue);
	                    }
	                }
	            }
	
	            logger.info("New messages to the DB inserted");
            }
        } catch (Exception e) {
            logger.error("Cannot add new messages", e);
        }
    }
}
