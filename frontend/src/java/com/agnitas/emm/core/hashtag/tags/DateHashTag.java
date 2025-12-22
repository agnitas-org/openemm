/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import com.agnitas.emm.core.hashtag.AbstractColonHashTagWithParameters;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.dao.DateFormatDao;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.TimeoutLRUMap;

public class DateHashTag extends AbstractColonHashTagWithParameters {

	private TimeoutLRUMap<Integer, String> dateFormatTypeCache = null;
	
	private DateFormatDao dateFormatDao;
	
	private ConfigService configService;

	public void setDateFormatDao(DateFormatDao dateFormatDao) {
		this.dateFormatDao = dateFormatDao;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Override
	public boolean isSupportedTag(final String tagName, final boolean hasColon) {
		return "date".equalsIgnoreCase(tagName);
	}
	
	@Override
	public String handleWithParametersInternal(final HashTagContext context, final String tagName, final Map<String, String> parameters) {
		final int type = Integer.parseInt(getMapValueWithDefault(parameters, "type", "0"));
		final String language = getMapValueWithDefault(parameters, "language", "de");
		final String country = getMapValueWithDefault(parameters, "country", "DE");
		final int offset = Integer.parseInt(getMapValueWithDefault(parameters, "offset", "0")) * 24 * 60 * 60;
		final String typeString = parameters.get("format") != null ? parameters.get("format") : getDateFormatTypeString(type);

		final Locale locale = new Locale(language, country);
		final SimpleDateFormat dateFormat = new SimpleDateFormat(typeString, locale);
		
		Date date = (new GregorianCalendar(locale)).getTime();
		if (offset != 0) {
			date = new Date(date.getTime() + (offset * 1000));
		}

		return dateFormat.format(date);
	}

	private static String getMapValueWithDefault(Map<String, String> parameters, String key, String defaultValue) {
		String value = parameters.get(key);
		return value != null ? value : defaultValue;
	}

	private String getDateFormatTypeString(int type) {
		String typestr = getDateFormatTypeCache().get(type);

		if (typestr == null) {
			typestr = dateFormatDao.getFormat(type);

			if (typestr == null) {
				typestr = "d.M.yyyy";
			}
			getDateFormatTypeCache().put(type, typestr);
		}
		return typestr;
	}

	private TimeoutLRUMap<Integer, String> getDateFormatTypeCache() {
		if (dateFormatTypeCache == null) {
			dateFormatTypeCache = new TimeoutLRUMap<>(configService.getIntegerValue(ConfigValue.RedirectKeysMaxCache), configService.getIntegerValue(ConfigValue.RedirectKeysMaxCacheTimeMillis));
		}
		return dateFormatTypeCache;
	}

}
