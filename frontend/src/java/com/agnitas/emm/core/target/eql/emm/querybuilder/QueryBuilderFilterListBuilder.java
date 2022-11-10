/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.SafeString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;

import net.sf.json.JSONSerializer;

/**
 * Converts the list of profile fields to the list of filters
 * used by the jQuery QueryBuilder.
 */
public class QueryBuilderFilterListBuilder {

	private static final Logger logger = LogManager.getLogger(QueryBuilderFilterListBuilder.class);
	
	private QueryBuilderConfiguration queryBuilderConfiguration;

	private ProfileFieldService profileFieldService;

	/**
	 * Creates the filter list as JSON string from profile fields for given company ID.
	 * 
	 * @param admin admin that's bound to profile fields
	 * 
	 * @return JSON string for the QueryBuilder filter list
	 * 
	 * @throws Exception
	 */
	public String buildFilterListJson(ComAdmin admin, boolean excludeHiddenFields) throws Exception {
		List<ProfileField> profileFields = listProfileFields(admin.getCompanyID(), admin.getAdminID(), excludeHiddenFields);
		createIndependentFilters(profileFields, admin);

		final List<Map<String, Object>> map = createFilterList(profileFields, admin);
		
		return JSONSerializer.toJSON(map).toString();
	}

	private void createIndependentFilters(final List<ProfileField> profileFields, final ComAdmin admin) {
		TargetRuleKey[] excludedRulesKeys;

		if (AgnUtils.isMailTrackingAvailable(admin)) {
			excludedRulesKeys = new TargetRuleKey[]{TargetRuleKey.FINISHED_AUTOEXPORT};
		} else {
			excludedRulesKeys = new TargetRuleKey[]{TargetRuleKey.RECEIVED_MAILING, TargetRuleKey.FINISHED_AUTOEXPORT};
		}

		profileFields.addAll(queryBuilderConfiguration.getIndependentFieldsExcluding(excludedRulesKeys));
	}

	/**
	 * Creates a list of filter settings for JSON encoding.
	 * 
	 * @param profileFields list of known profile fields
	 * 
	 * @return list of filter settings for JSON encoding
	 */
	private List<Map<String, Object>> createFilterList(final List<ProfileField> profileFields, ComAdmin admin) {

		// TODO: Respect unknown profile fields somehow
		
		final List<Map<String, Object>> filterList = new ArrayList<>();
		
		for(ProfileField profileField : profileFields) {
			final Map<String, Object> filter = new HashMap<>();
			
			filter.put("id", profileField.getShortname().toLowerCase());
			filter.put("label", getLabel(profileField, admin.getLocale()));
			
			final SimpleDataType dataType = DbColumnType.getSimpleDataType(profileField.getDataType(), profileField.getNumericScale());
			switch(dataType) {
			case Numeric:
			case Float:
				filter.put("type", "double");
				break;
				
			case Date:
			case DateTime:
				filter.put("type", "date");
				break;
				
			case Characters:
				filter.put("type", "string");
				break;
			default:
			}
			
			filterList.add(filter);
		}
		
		return filterList;
	}

	private String getLabel(ProfileField profileField, Locale locale) {
		String label = profileField.getLabel();
		return StringUtils.isNotBlank(label) ? SafeString.getLocaleString(label, locale) : profileField.getShortname();
	}

	/**
	 * Lists all known profile fields for given company ID.
	 * 
	 * @param companyID company ID
	 * 
	 * @return list of all known profile fields
	 * 
	 * @throws Exception
	 */
	private List<ProfileField> listProfileFields(int companyID, int adminId, boolean excludeHidden) throws Exception {
		if (excludeHidden) {
			return profileFieldService.getProfileFields(companyID, adminId).stream().filter(x -> x.getModeEdit() != ProfileField.MODE_EDIT_NOT_VISIBLE).collect(Collectors.toList());
		}

		try {
			return profileFieldService.getProfileFields(companyID);
		} catch(final Exception e) {
			logger.error("Error listing profile fields", e);

			throw new QueryBuilderFilterListBuilderException("Error listing profile fields", e);
		}
	}
	
	// ------------------------------------------------------------------------------------------------------------------ Dependency Injection

	@Required
	public void setQueryBuilderConfiguration(QueryBuilderConfiguration queryBuilderConfiguration) {
		this.queryBuilderConfiguration = queryBuilderConfiguration;
	}

	@Required
	public void setProfileFieldService(ProfileFieldService profileFieldService) {
		this.profileFieldService = profileFieldService;
	}
}
