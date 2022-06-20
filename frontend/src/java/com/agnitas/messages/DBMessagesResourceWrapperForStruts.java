/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.messages;

import java.util.Locale;

import org.apache.struts.util.MessageResources;
import org.apache.struts.util.MessageResourcesFactory;

public class DBMessagesResourceWrapperForStruts extends MessageResources {
	/** Serial version UID. */
	private static final long serialVersionUID = -8035186847063135652L;
	
	private DBMessagesResource dbMessagesResource;

	/**
	 * Constructor
	 * 
	 * @param factory
	 * @param config
	 */
	public DBMessagesResourceWrapperForStruts(MessageResourcesFactory factory, String config) {
		super(factory, config);
	}
	
	public void init() {
		if (I18nString.MESSAGE_RESOURCES == null) {
			dbMessagesResource = new DBMessagesResource();
			dbMessagesResource.init();
		} else {
			// Message properties have already been initialized
			dbMessagesResource = I18nString.MESSAGE_RESOURCES;
		}
	}

	/**
	 * Get message for internationalisation (i18n)
	 */
	@Override
	public String getMessage(Locale locale, String key) {
		return getMessage(locale.getLanguage(), key);
	}

	/**
	 * Get message for internationalisation (i18n)
	 */
	public String getMessage(String language, String key) {
		return dbMessagesResource.getMessage(language, key);
	}
}
