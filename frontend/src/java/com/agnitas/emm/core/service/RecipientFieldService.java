/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import org.agnitas.emm.core.commons.util.ConfigValue;

public interface RecipientFieldService {

	enum RecipientOptionalField {
		FrequencyCountDay("freq_count_day"),
		FrequencyCounterWeek("freq_count_week"),
		FrequencycountMonth("freq_count_month");

		private String columnName;
		
		RecipientOptionalField(String columnName) {
			this.columnName = columnName.toLowerCase();
		}
		
		public String getColumnName() {
			return columnName;
		}
	}
	
	/**
	 * Special system profile field for sending letters by postal services
	 */
	enum PostalField {
	    COUNTRY("company.settings.post.country", ConfigValue.DefaultCountryField),
	    CITY("company.settings.post.city", ConfigValue.DefaultCityField),
	    STREET("company.settings.post.street", ConfigValue.DefaultStreetField),
	    CODE("company.settings.post.code", ConfigValue.DefaultPostalCodeField);

	    private final ConfigValue configValue;
	    private final String messageKey;

	    PostalField(String messageKey, ConfigValue configValue) {
	        this.messageKey = messageKey;
	        this.configValue = configValue;
	    }

	    public ConfigValue getConfigValue() {
	        return configValue;
	    }

	    public String getMessageKey() {
	        return messageKey;
	    }
	}
	
	/**
	 * Socialmedia fields to be ignored in limit checks for profile field counts until they are removed entirely in all client tables
	 */
	List<String> OLD_SOCIAL_MEDIA_FIELDS = Arrays.asList(
		"facebook_status",
		"foursquare_status",
		"google_status",
		"twitter_status",
		"xing_status"
	);

	Set<String> getStandardFieldsNames(int companyId);
	List<RecipientFieldDescription> getRecipientFields(int companyID);
	List<RecipientFieldDescription> getEditableFields(int companyId);
	List<RecipientFieldDescription> getHistorizedFields(int companyId);
	List<RecipientFieldDescription> getRecipientFields(ProfileFieldForm profileForm, int companyId);
	Map<String, String> getRecipientDBStructure(int companyID);
	RecipientFieldDescription getRecipientField(int companyID, String recipientFieldName);
	void saveRecipientField(int companyID, RecipientFieldDescription recipientFieldDescription) throws Exception;
	void deleteRecipientField(int companyID, String recipientFieldName) throws Exception;
	boolean isReservedKeyWord(String fieldname);
	void clearCachedData(int companyID);
	boolean hasRecipients(int companyID);
	boolean hasRecipientsWithNullValue(int companyID, String columnName);
    boolean mayAddNewRecipientField(int companyID);
	int countCustomerEntries(int companyID);
	boolean checkAllowedDefaultValue(int companyID, String fieldname, String fieldDefault);
	int getClientSpecificFieldCount(int companyID);
	ServiceResult<List<String>> filterAllowedForDelete(Map<String, SimpleServiceResult> validationResults, Admin admin);
	ServiceResult<UserAction> delete(Map<String, SimpleServiceResult> validationResults, Admin admin);
	long getCountForOverview(int companyId);
    List<RecipientFieldDescription> getHistorizedCustomFields(int companyId);
    Map<String, String> getEditableFieldsMap(int companyId);
}
