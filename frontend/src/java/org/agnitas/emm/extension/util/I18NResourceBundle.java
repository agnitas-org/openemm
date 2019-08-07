/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class I18NResourceBundle {

	private final I18NFactory factory;
	private final Map<String, ResourceBundle> messages;
	
	
	public I18NResourceBundle( I18NFactory factory) {
		this.factory = factory;
		this.messages = new HashMap<>();
	}

	public String getMessage(String key, Locale locale) throws Exception {
		ResourceBundle languageBundle = getMessageBundle( locale.getLanguage() + "_" + locale.getCountry());		
		if( languageBundle != null && languageBundle.containsKey( key))
			return languageBundle.getString( key);
		
		languageBundle = getMessageBundle( locale.getLanguage());
		if( languageBundle != null && languageBundle.containsKey( key))
			return languageBundle.getString( key);

		languageBundle = getMessageBundle( "");
		if( languageBundle != null && languageBundle.containsKey( key))
			return languageBundle.getString( key);
		
		return null;
	}
	
	private ResourceBundle getMessageBundle(String i18nPrefix) throws Exception {
		ResourceBundle bundle = messages.get( i18nPrefix);
		
		if (bundle == null) {
			bundle = factory.getMessages(i18nPrefix);
		
			messages.put( i18nPrefix, bundle);
		}
		
		return bundle;
	}
}
