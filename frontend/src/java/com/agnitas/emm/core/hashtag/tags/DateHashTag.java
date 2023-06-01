/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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

import org.agnitas.dao.DateFormatDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.TimeoutLRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.hashtag.AbstractColonHashTagWithParameters;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.exception.HashTagException;

public class DateHashTag extends AbstractColonHashTagWithParameters {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(DateHashTag.class);
	
//	private static final Pattern HASHTAG_PARAMETER_PATTERN = Pattern.compile("^\\s*(\\w+)\\s*=\\s*\'([^\']*)\'\\s*(?:,(.*))?$");
	
	private TimeoutLRUMap<Integer, String> dateFormatTypeCache = null;
	
	private DateFormatDao dateFormatDao;
	
	private ConfigService configService;

	@Required
	public void setDateFormatDao(DateFormatDao dateFormatDao) {
		this.dateFormatDao = dateFormatDao;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Override
	public boolean isSupportedTag(final String tagName, final boolean hasColon) {
		return "date".equalsIgnoreCase(tagName);
	}
	
	@Override
	public String handleWithParametersInternal(final HashTagContext context, final String tagName, final Map<String, String> parameters) throws HashTagException {
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

//	private static Map<String, String> getPlaceholderParameters(String parameterString) {
//		Map<String, String> parameters = new HashMap<>();
//
//		Matcher matcher = HASHTAG_PARAMETER_PATTERN.matcher(parameterString);
//		while (parameterString != null && matcher.matches()) {
//			String key = matcher.group(1);
//			String value = matcher.group(2);
//
//			parameters.put(key, value);
//			parameterString = matcher.group(3);
//
//			if (parameterString != null) {
//				matcher = HASHTAG_PARAMETER_PATTERN.matcher(parameterString);
//			}
//		}
//
//		return parameters;
//	}

	private static String getMapValueWithDefault(Map<String, String> parameters, String key, String defaultValue) {
		String value = parameters.get(key);
		return value != null ? value : defaultValue;
	}

	private String getDateFormatTypeString(int type) {
		String typestr = getDateFormatTypeCache().get(type);

		if (typestr == null) {
			try {
				typestr = dateFormatDao.getFormat(type);
			} catch (Exception e) {
				logger.warn("Error getting date format from database", e);
			}

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
