/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.dao.RecipientDao;
import com.agnitas.dao.TagDao;
import com.agnitas.emm.core.hashtag.HashTag;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.exception.HashTagException;
import com.agnitas.util.TimeoutLRUMap;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;

/**
 * Should not be longer used.
 * 
 * Left here for backward compatibility.
 * 
 * @see AgeHashTag
 */
@Deprecated
public class AlterHashTag implements HashTag { // TODO Derive from AbstractColonHashTag instead of implementing HashTag

	private static final Pattern HASHTAG_PARAMETER_PATTERN = Pattern.compile("^\\s*(\\w+)\\s*=\\s*\'([^\']*)\'\\s*(?:,(.*))?$");
	
	private TagDao tagDao;
	
	private RecipientDao recipientDao;
	
	private ConfigService configService;
	
	private TimeoutLRUMap<Integer, String> tagSelectColumnCache = null;

	public void setTagDao(TagDao tagDao) {
		this.tagDao = tagDao;
	}

	public void setRecipientDao(RecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Override
	public boolean canHandle(HashTagContext context, String tagString) {
		return "alter".equalsIgnoreCase(tagString) || tagString.toLowerCase().startsWith("alter:");
	}

	@Override
	public String handle(HashTagContext hashTagContext, String hashTagContent) throws HashTagException {
		Map<String, String> parameters;
		if (hashTagContent.startsWith("ALTER:")) {
			parameters = getPlaceholderParameters(hashTagContent.substring(6).trim());
		} else if (hashTagContent.equals("ALTER")) {
			parameters = new HashMap<>();
		} else {
			throw new HashTagException("Invalid tagname for AlterHashTag");
		}
		
		String column = parameters.get("column");
		if (column != null) {
			String selectValue = getTagSelectColumnCache().get(hashTagContext.getCompanyID());
			if (selectValue == null) {
				selectValue = tagDao.getTag(hashTagContext.getCompanyID(), "agnALTER").getSelectValue().replace("{column}", column);
				getTagSelectColumnCache().put(hashTagContext.getCompanyID(), selectValue);
			}
			return recipientDao.selectCustomerValue(selectValue, hashTagContext.getCompanyID(), hashTagContext.getCustomerId());
		} else {
			return "";
		}
	}

	private static Map<String, String> getPlaceholderParameters(String parameterString) {
		Map<String, String> parameters = new HashMap<>();
		
		Matcher matcher = HASHTAG_PARAMETER_PATTERN.matcher(parameterString);
		while (parameterString != null && matcher.matches()) {
			String key = matcher.group(1);
			String value = matcher.group(2);

			parameters.put(key, value);
			parameterString = matcher.group(3);

			if (parameterString != null) {
				matcher = HASHTAG_PARAMETER_PATTERN.matcher(parameterString);
			}
		}
		
		return parameters;
	}

	private TimeoutLRUMap<Integer, String> getTagSelectColumnCache() {
		if (tagSelectColumnCache == null) {
			tagSelectColumnCache = new TimeoutLRUMap<>(configService.getIntegerValue(ConfigValue.RedirectKeysMaxCache), configService.getIntegerValue(ConfigValue.RedirectKeysMaxCacheTimeMillis));
		}
		return tagSelectColumnCache;
	}
}
