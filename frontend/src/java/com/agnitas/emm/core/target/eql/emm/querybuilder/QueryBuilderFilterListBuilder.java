/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.DbColumnType.SimpleDataType;
import com.agnitas.util.SafeString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

/**
 * Converts the list of profile fields to the list of filters
 * used by the jQuery QueryBuilder.
 */
public class QueryBuilderFilterListBuilder {

	private static final Logger logger = LogManager.getLogger(QueryBuilderFilterListBuilder.class);

	private ProfileFieldService profileFieldService;
	private QueryBuilderConfiguration queryBuilderConfiguration;

	/**
	 * Creates the filter list as JSON string from profile fields for given company ID.
	 *
	 * @param admin admin that's bound to profile fields
	 *
	 * @return JSON string for the QueryBuilder filter list
	 */
	public String buildFilterListJson(Admin admin, boolean excludeHiddenFields) {
		List<ProfileField> profileFields = listProfileFields(admin.getCompanyID(), admin.getAdminID(), excludeHiddenFields);
		createIndependentFilters(profileFields, admin);

		final List<Map<String, Object>> map = createFilterList(profileFields, admin);
		return new JSONArray(map).toString();
	}

	private void createIndependentFilters(final List<ProfileField> profileFields, final Admin admin) {
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
	private List<Map<String, Object>> createFilterList(final List<ProfileField> profileFields, Admin admin) {
		// TODO: Respect unknown profile fields somehow

		final List<Map<String, Object>> filterList = new ArrayList<>();

		for (ProfileField profileField : profileFields) {
			final Map<String, Object> filter = new HashMap<>();

			filter.put("id", profileField.getShortname().toLowerCase());
			filter.put("label", getLabel(profileField, admin.getLocale()));

			final SimpleDataType dataType = DbColumnType.getSimpleDataType(profileField.getDataType(), profileField.getNumericScale());
			switch(dataType) {
				case Numeric, Float:
			    	filter.put("type", "double");
			    	break;

				case Date, DateTime:
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
	 */
	private List<ProfileField> listProfileFields(int companyID, int adminID, boolean excludeHidden) {
		if (excludeHidden) {
			return profileFieldService.getProfileFields(companyID, adminID).stream().filter(x -> x.getModeEdit() != ProfileFieldMode.NotVisible).collect(Collectors.toList());
		}

		return profileFieldService.getProfileFields(companyID);
	}

	// ------------------------------------------------------------------------------------------------------------------ Dependency Injection

	public void setQueryBuilderConfiguration(QueryBuilderConfiguration queryBuilderConfiguration) {
		this.queryBuilderConfiguration = queryBuilderConfiguration;
	}

	public void setProfileFieldService(ProfileFieldService profileFieldService) {
		this.profileFieldService = profileFieldService;
	}
}
