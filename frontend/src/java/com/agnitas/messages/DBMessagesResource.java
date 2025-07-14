/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.messages;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NoInitialContextException;
import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.dao.MessageDao;
import com.agnitas.dao.impl.MessageDaoImpl;
import com.agnitas.util.Version;

/**
 * Class to use MessageResources from db
 * 
 * Watchout:
 * 	Default language "en" is listed as "default" within the map
 */
public class DBMessagesResource {

	private static final Logger logger = LogManager.getLogger(DBMessagesResource.class);

	private static final String DEFAULT_LANGUAGE = "en";
	private static final String DEFAULT_LANGUAGE_MAPKEY = "default";
	
	/**
	 * Import new messages from new_messages.properties file
	 */
	private static final boolean IMPORT_NEWMESSAGEPROPERTIES = true;

	private Map<String, Map<String, String>> allMessages = null;
	
	private List<String> allMessageKeys = null;
	
	private MessageDao messageDao;

	public MessageDao getMessageDao() throws Exception {
		if (messageDao == null) {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");

			DataSource dataSource = (DataSource) envCtx.lookup("jdbc/emm_db");

			messageDao = new MessageDaoImpl();
			((MessageDaoImpl) messageDao).setDataSource(dataSource);
		}
		return messageDao;
	}

	public void setMessageDao(MessageDao messageDao) {
		this.messageDao = messageDao;
	}
	
	public void init() {
		try {
			ConfigService configService = ConfigService.getInstance();
			
			boolean readDeletedMessages = true;
			List<String> hostsToIgnoreDeletedMessages = configService.getListValue(ConfigValue.IgnoreDeletedI18NMessagesHosts);
			for (String hostToIgnoreDeletedMessages : hostsToIgnoreDeletedMessages) {
				if (hostToIgnoreDeletedMessages.equalsIgnoreCase(AgnUtils.getHostName())) {
					readDeletedMessages = false;
					break;
				}
			}
			
			if (IMPORT_NEWMESSAGEPROPERTIES) {
				NewMessagesPropertiesImporter.importNewMessagesProperties(getMessageDao());
			}

			String applicationVersionString = configService.getValue(ConfigValue.ApplicationVersion);
			Version applicationVersion;
			try {
				applicationVersion = new Version(applicationVersionString);
			} catch (Exception e) {
				// This may happen on trunk builds, so just write a warning and show a default version
				logger.warn("Invalid application version '" + applicationVersionString + "': " + e.getMessage());
				applicationVersion = new Version("0");
			}
			
			allMessages = getMessageDao().getAllMessages(readDeletedMessages);
			allMessageKeys = getMessageDao().getAllMessageKeys(readDeletedMessages);
			
			Map<String, String> defaultLanguageMessages = allMessages.get(DEFAULT_LANGUAGE_MAPKEY);

			// Add html version text to be used near the logo
			String versionString;
			String redesignedVersionString;
			if (configService.getBooleanValue(ConfigValue.IsBetaInstance)) {
				versionString = "<b><font color=\"#FF0000\">BETA</font></b> ";
				redesignedVersionString = "<span class=\"text-danger\">BETA</span>";
			} else if (configService.getBooleanValue(ConfigValue.IsLegacyInstance)) {
				versionString = "<b><font color=\"#FF0000\">LEGACY</font></b> ";
				redesignedVersionString = "<span class=\"text-danger\">LEGACY</span>";
			} else {
				versionString = "";
				redesignedVersionString = "";
			}
			int minorVersion = applicationVersion.getMinorVersion();
			if (4 == minorVersion) {
				versionString += "SPRING ";
				redesignedVersionString += "SPRING ";
			} else if (7 == minorVersion) {
				versionString += "SUMMER ";
				redesignedVersionString += "SUMMER ";
			} else if (10 == minorVersion) {
				versionString += "FALL ";
				redesignedVersionString += "FALL ";
			}
			if (applicationVersion.getMajorVersion() > 0) {
				versionString += "20" + applicationVersion.getMajorVersion();
				redesignedVersionString += "20" + applicationVersion.getMajorVersion();
			} else {
				versionString += "<b><font color=\"#FF0000\">TEST</font></b>";
				redesignedVersionString += "<span class=\"text-danger\">TEST</span>";
			}
			// TODO: EMMGUI-714: try to remove default.version after remove of old design. (if possible)
			defaultLanguageMessages.put("default.version", versionString);
			defaultLanguageMessages.put("default.version.redesigned", redesignedVersionString);

			// Replace all version placeholders in messages
			// This must be done here and not via db, because the same db is used by different application versions as "beta" etc.
			for(final Map.Entry<String, Map<String, String>> entry0 : allMessages.entrySet()) {
				Map<String, String> messages = entry0.getValue();
				
				for(final Map.Entry<String, String> entry : messages.entrySet()) {
					if (entry.getValue() != null) {
						String message = entry.getValue().replace("\\n", "\n");
						messages.put(entry.getKey(), AgnUtils.replaceVersionPlaceholders(message, applicationVersion));
					}
				}
			}
			
			rewritePropertiesWithVersionTag(allMessages, applicationVersion);
			
			logger.info("MessageResources successfully loaded");
			
			I18nString.MESSAGE_RESOURCES = this;
		} catch (NoInitialContextException e) {
			// This happens when tests cannot find a JNDI environment
			logger.error("MessageResources cannot find JNDI environment");
		} catch (Exception e) {
			logger.error("MessageResources load failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Get message for internationalisation (i18n)
	 */
	public String getMessage(Locale locale, String key) {
		if (locale == null) {
			return getMessage(DEFAULT_LANGUAGE, key);
		} else {
			return getMessage(locale.getLanguage(), key);
		}
	}

	/**
	 * Get message for internationalisation (i18n)
	 */
	public String getMessage(String language, String key) {
		if (DEFAULT_LANGUAGE.equals(language)) {
			language = DEFAULT_LANGUAGE_MAPKEY;
		}
		
		String fallbackLanguage = null;
		if (language.contains("_")) {
			fallbackLanguage = language.substring(0, language.indexOf("_"));
		}
		
		if (allMessages == null || allMessages.size() == 0) {
			logger.error("[message resources not loaded]");
			return "[message resources not loaded]";
		} else if (allMessages.containsKey(language) && allMessages.get(language).containsKey(key) && allMessages.get(language).get(key) != null) {
			return allMessages.get(language).get(key);
		} else if (fallbackLanguage != null && allMessages.containsKey(fallbackLanguage) && allMessages.get(fallbackLanguage).containsKey(key) && allMessages.get(fallbackLanguage).get(key) != null) {
			return allMessages.get(fallbackLanguage).get(key);
		} else if (allMessages.containsKey(DEFAULT_LANGUAGE_MAPKEY) && allMessages.get(DEFAULT_LANGUAGE_MAPKEY).containsKey(key) && allMessages.get(DEFAULT_LANGUAGE_MAPKEY).get(key) != null) {
			return allMessages.get(DEFAULT_LANGUAGE_MAPKEY).get(key);
		} else {
			logger.error("[missing message for key \"" + key + "\"]", new Exception()); // new Exception() for Stacktrace in log
			return "[missing message for key \"" + key + "\"]";
		}
	}
	
	public boolean hasMessageForKey(String key) {
		if (allMessages != null && allMessages.size() > 0 && allMessages.containsKey(DEFAULT_LANGUAGE_MAPKEY) && allMessages.get(DEFAULT_LANGUAGE_MAPKEY).containsKey(key) && allMessages.get(DEFAULT_LANGUAGE_MAPKEY).get(key) != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public ResourceBundle getMessagesAsResourceBundle(String language) {
		ResourceBundle returnBundle = new MapResourceBundle(allMessages.get(language));
		return returnBundle;
	}

	private void rewritePropertiesWithVersionTag(Map<String, Map<String, String>> messagesToCheck, Version applicationVersion) throws Exception {
		try {
			String logonTitle = messagesToCheck.get(DEFAULT_LANGUAGE_MAPKEY).get("logon.title");
			if (logonTitle == null) {
				throw new Exception("Message logon.title is missing. Maybe all messages are missing?");
			} else {
				logonTitle = AgnUtils.replaceVersionPlaceholders(logonTitle, applicationVersion);
			}

			ConfigService configService = ConfigService.getInstance();
			
			if (configService.getBooleanValue(ConfigValue.IsLiveInstance)) {
				// Remove the versionsign from live server sites
				messagesToCheck.get(DEFAULT_LANGUAGE_MAPKEY).put("versionsign", "");
			} else {
				String versionsign = messagesToCheck.get(DEFAULT_LANGUAGE_MAPKEY).get("versionsign");
				if (versionsign != null) {
					versionsign = AgnUtils.replaceVersionPlaceholders(versionsign, applicationVersion);
				}
				String systemTypeSign = "";
				if (configService.getBooleanValue(ConfigValue.IsBetaInstance)) {
					systemTypeSign = " BETA";
				} else if (configService.getBooleanValue(ConfigValue.IsLegacyInstance)) {
					systemTypeSign = " LEGACY";
				}
				messagesToCheck.get(DEFAULT_LANGUAGE_MAPKEY).put("versionsign", versionsign + systemTypeSign + " (" + applicationVersion.toString() + ")");
			}
		} catch (Exception e) {
			throw new Exception("Error while rewritePropertiesWithVersionTag: " + e.getMessage(), e);
		}
	}
	
	public List<String> getAvailableKeys() {
		return allMessageKeys;
	}
	
	public Map<String, Map<String, String>> getAllMessages() {
		return allMessages;
	}
}
