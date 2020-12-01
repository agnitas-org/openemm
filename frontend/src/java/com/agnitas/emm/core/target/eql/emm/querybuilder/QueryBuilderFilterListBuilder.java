/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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

import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.SafeString;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ComProfileFieldDao;

import net.sf.json.JSONSerializer;

/**
 * Converts the list of profile fields to the list of filters
 * used by the jQuery QueryBuilder.
 */
public class QueryBuilderFilterListBuilder {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(QueryBuilderFilterListBuilder.class);
	
	/** DAO accessing profile fields. */
	private ComProfileFieldDao profileFieldDao;

	private QueryBuilderConfiguration queryBuilderConfiguration;

	/**
	 * Creates the filter list as JSON string from profile fields for given company ID.
	 * 
	 * @param admin admin that's bound to profile fields
	 * 
	 * @return JSON string for the QueryBuilder filter list
	 * 
	 * @throws QueryBuilderFilterListBuilderException on errors creating filter list
	 */
	public String buildFilterListJson(final ComAdmin admin) throws QueryBuilderFilterListBuilderException {
		final List<ProfileField> profileFields = listProfileFields(admin.getCompanyID());
		createIndependentFilters(profileFields, admin);

		final List<Map<String, Object>> map = createFilterList(profileFields, admin);
		
		return JSONSerializer.toJSON(map).toString();
	}

	private void createIndependentFilters(final List<ProfileField> profileFields, final ComAdmin admin) {
		profileFields.addAll(queryBuilderConfiguration.getIndependentFields(AgnUtils.isMailTrackingAvailable(admin)));
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
	 * @throws QueryBuilderFilterListBuilderException on errors listing profile fields
	 */
	private List<ProfileField> listProfileFields(final int companyID) throws QueryBuilderFilterListBuilderException {
		try {
			return profileFieldDao.getComProfileFields(companyID);
		} catch(final Exception e) {
			logger.error("Error listing profile fields", e);
			
			throw new QueryBuilderFilterListBuilderException("Error listing profile fields", e);
		}
	}
	
	// ------------------------------------------------------------------------------------------------------------------ Dependency Injection
	/**
	 * Set DAO accessing profile field data.
	 * 
	 * @param dao DAO accessing profile field data
	 */
	@Required
	public void setProfileFieldDao(final ComProfileFieldDao dao) {
		this.profileFieldDao = dao;
	}

	@Required
	public void setQueryBuilderConfiguration(QueryBuilderConfiguration queryBuilderConfiguration) {
		this.queryBuilderConfiguration = queryBuilderConfiguration;
	}
}
