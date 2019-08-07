/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.birt.report.resource.BirtResources;

import com.agnitas.messages.I18nString;

public class ComBirtResources {
    private static Map<Locale, ComBirtResourceHandle> resourceMap = new HashMap<>();
    
    public static String getMessage(String key) {
        return getMessage(key, getLocale());
    }

    public static String getMessage(String key, Locale locale) {
    	if (I18nString.hasMessageForKey(key)) {
    		return I18nString.getLocaleString(key, locale);
    	} else if (BirtResources.getMessage(key) != null) {
    		// Use Birt-internal message texts for keys like "birt.viewer.dialog.ok"
    		return BirtResources.getMessage(key);
    	} else {
    		// Returns error message text for missing key
    		return I18nString.getLocaleString(key, locale);
    	}
    }

    public static String getJavaScriptMessage(String key) {
        return BirtResources.makeJavaScriptString(getMessage(key, getLocale()));
    }

    public static String getJavaScriptMessage(String key, Locale locale) {
        return BirtResources.makeJavaScriptString(getMessage(key, locale));
    }

    public static String getHtmlMessage(String key) {
        return BirtResources.makeHtmlString(getMessage(key, getLocale()));
    }

    public static String getHtmlMessage(String key, Locale locale) {
        return BirtResources.makeHtmlString(getMessage(key, locale));
    }

    public static ComBirtResourceHandle getBirtResourceHandle(Locale locale) {
        ComBirtResourceHandle resourceHandle = resourceMap.get(locale);
        if (resourceHandle != null) {
            return resourceHandle;
        }
        synchronized (resourceMap) {
            if (resourceMap.get(locale) != null){
                return resourceMap.get(locale);
            }
            resourceHandle = new ComBirtResourceHandle(locale);
            resourceMap.put(locale, resourceHandle);
        }
        return resourceHandle;
    }

    public static Locale getLocale() {
        return BirtResources.getLocale();
    }
}
