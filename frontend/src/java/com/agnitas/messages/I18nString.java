/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.messages;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class I18nString {
	public static DBMessagesResource MESSAGE_RESOURCES = null;

    /**
     * Gets a locale string.
     */
	public static String getLocaleString(String key, Locale locale) {
		if (MESSAGE_RESOURCES == null) {
			throw new RuntimeException("DBMessagesResource was not initialized properly");
		} else {
			return MESSAGE_RESOURCES.getMessage(locale, key);
		}
	}

    /**
     * Checks for key correctness and retrieves translation.
     *
     * @param key          key of necessary translation.
     * @param locale       language.
     * @param defaultValue value returns in case of translation not exist.
     * @return ether translation by current key or defaultValue.
     */
    public static String getLocaleStringOrDefault(String key, Locale locale, String defaultValue) {
        if (StringUtils.isBlank(key) || !hasMessageForKey(key)) {
            return defaultValue;
        }

        return getLocaleString(key, locale);
    }

    /**
     * Gets a locale string and replaces text placeholders
     */
	public static String getLocaleString(String key, Locale locale, Object... parameters) {
		String message = getLocaleString(key, locale);
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] != null) {
					message = message.replace("{" + i + "}", parameters[i].toString());
				} else {
					message = message.replace("{" + i + "}", "");
				}
			}
		}
		return message;
	}

    /**
     * Gets a locale string.
     */
	public static String getLocaleString(String key, String language) {
		return getLocaleString(key, language == null ? null : new Locale(language));
	}

    /**
     * Gets a locale string.
     */
	public static String getLocaleString(String key, String language, Object... parameters) {
		return getLocaleString(key, language == null ? null : new Locale(language), parameters);
	}

	public static boolean hasMessageForKey(String key) {
		return MESSAGE_RESOURCES.hasMessageForKey(key);
	}
}
