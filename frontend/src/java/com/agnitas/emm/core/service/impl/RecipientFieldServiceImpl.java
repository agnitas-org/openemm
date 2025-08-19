/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.service.impl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.dao.RecipientFieldDao;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;
import com.agnitas.emm.core.recipient.service.RecipientProfileHistoryService;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientFieldsCache;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.Const;
import com.agnitas.util.DbColumnType;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipientFieldServiceImpl implements RecipientFieldService {

	private static final Logger logger = LogManager.getLogger(RecipientFieldServiceImpl.class);

	private ConfigService configService;
	private RecipientFieldsCache recipientFieldsCache;
	private RecipientFieldDao recipientFieldDao;
	private ProfileFieldDao profileFieldDao;
	private RecipientProfileHistoryService recipientProfileHistoryService;
	private BulkActionValidationService<String, String> bulkActionValidationService;

	public Set<String> getStandardFieldsNames(int companyId) {
		return getRecipientFields(companyId)
				.stream()
				.filter(RecipientFieldDescription::isStandardField)
				.map(RecipientFieldDescription::getColumnName)
				.collect(Collectors.toSet());
	}

	@Override
	public List<RecipientFieldDescription> getRecipientFields(int companyID) {
		return getCachedRecipientFieldsData(companyID);
	}

	@Override
	public List<RecipientFieldDescription> getRecipientFields(ProfileFieldForm profileForm, int companyId) {
		List<RecipientFieldDescription> fields = getRecipientFields(companyId);

		String fieldName = profileForm.getFilterFieldName();
		String dbFieldName = profileForm.getFilterDbFieldName();
		String description = profileForm.getFilterDescription();
		DbColumnType.SimpleDataType type = profileForm.getFilterType();
		ProfileFieldMode mode = profileForm.getFilterMode();
		Boolean historized = profileForm.getHistorized();
		return fields.stream()
				.filter(f -> StringUtils.isBlank(fieldName) || f.getShortName().toLowerCase().contains(fieldName.toLowerCase()))
				.filter(f -> StringUtils.isBlank(dbFieldName) || f.getColumnName().toLowerCase().contains(dbFieldName.toLowerCase()))
				.filter(f -> StringUtils.isBlank(description) || (f.getDescription() != null && f.getDescription().toLowerCase().contains(description.toLowerCase())))
				.filter(f -> type == null || type.equals(f.getSimpleDataType()))
				.filter(f -> mode == null || mode.equals(f.getDefaultPermission()))
				.filter(f -> historized == null || historized.equals(f.isHistorized()))
				.collect(Collectors.toList());
	}

	@Override
	public List<RecipientFieldDescription> getEditableFields(int companyId) {
		return getRecipientFields(companyId).stream()
				.filter(t -> EnumSet.of(ProfileFieldMode.Editable).contains(t.getDefaultPermission()))
				.toList();
	}

	@Override
	public List<RecipientFieldDescription> getHistorizedFields(int companyId) {
		Set<String> historizedStandardFields = RecipientStandardField.getHistorizedRecipientStandardFieldColumnNames();
		return getRecipientFields(companyId).stream()
			.filter(f -> f.isHistorized() || historizedStandardFields.contains(f.getColumnName()))
			.toList();
	}

	@Override
	public Map<String, String> getEditableFieldsMap(int companyId) {
		return getEditableFields(companyId)
				.stream()
				.collect(Collectors.toMap(RecipientFieldDescription::getColumnName, RecipientFieldDescription::getShortName));
	}

	@Override
	public RecipientFieldDescription getRecipientField(int companyID, String recipientFieldName) {
		List<RecipientFieldDescription> recipientFields = getCachedRecipientFieldsData(companyID);
		for (RecipientFieldDescription recipientFieldDescription : recipientFields) {
			if (recipientFieldName.equalsIgnoreCase(recipientFieldDescription.getColumnName())
				|| recipientFieldName.equalsIgnoreCase(recipientFieldDescription.getShortName())) {
				return recipientFieldDescription;
			}
		}
		return null;
	}

	@Override
	public void saveRecipientField(int companyID, RecipientFieldDescription recipientFieldDescription) throws Exception {
		if (RecipientStandardField.Bounceload.getColumnName().equalsIgnoreCase(recipientFieldDescription.getColumnName())) {
			throw new Exception("Recipient field bounceload is unchangeable");
		} else {
			recipientFieldDao.saveRecipientField(companyID, recipientFieldDescription);

			clearCachedData(companyID);
			
			if (configService.isRecipientProfileHistoryEnabled(companyID)) {
				recipientProfileHistoryService.afterProfileFieldStructureModification(companyID);
			}
		}
	}

	@Override
	public void deleteRecipientField(int companyID, String recipientFieldName) throws Exception {
		if (RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(recipientFieldName)) {
			throw new Exception("Cannot delete recipient standard field: " + recipientFieldName);
		} else {
			recipientFieldDao.deleteRecipientField(companyID, recipientFieldName);

			clearCachedData(companyID);
			
			if (configService.isRecipientProfileHistoryEnabled(companyID)) {
				recipientProfileHistoryService.afterProfileFieldStructureModification(companyID);
			}
		}
	}

	@Override
	public void clearCachedData(int companyID) {
		recipientFieldsCache.put(companyID, null);
		profileFieldDao.clearProfileStructureCache(companyID);
	}

	private List<RecipientFieldDescription> getCachedRecipientFieldsData(int companyID) {
		List<RecipientFieldDescription> recipientFields = recipientFieldsCache.get(companyID);
		if (recipientFields == null) {
			recipientFields = recipientFieldDao.getRecipientFields(companyID).stream()
				.filter(recipientField -> !recipientField.getColumnName().equalsIgnoreCase(RecipientStandardField.Bounceload.getColumnName()))
				.collect(Collectors.toList());
			
			recipientFieldsCache.put(companyID, recipientFields);
		}
		return recipientFields;
	}

	@Override
	public boolean isReservedKeyWord(String fieldname) {
		return recipientFieldDao.isReservedKeyWord(fieldname);
	}
	
	@Override
	public Map<String, String> getRecipientDBStructure(int companyID) {
		Map<String, String> returnMap = new HashMap<>();
		for (RecipientFieldDescription field : getRecipientFields(companyID)) {
			returnMap.put(field.getColumnName(), field.getSimpleDataType().getGenericDbDataTypeName());
		}
		return returnMap;
	}

	@Override
	public boolean hasRecipients(int companyID) {
		return recipientFieldDao.hasRecipients(companyID);
	}

	@Override
	public boolean hasRecipientsWithNullValue(int companyID, String columnName) {
		return recipientFieldDao.hasRecipientsWithNullValue(companyID, columnName);
	}

	@Override
	public boolean mayAddNewRecipientField(int companyID) {
		if (companyID <= 0) {
    		return false;
    	} else {
    		int maxFields;
    		int systemMaxFields = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfProfileFields, companyID);
    		int companyMaxFields = configService.getIntegerValue(ConfigValue.MaxFields, companyID);
    		if (companyMaxFields >= 0 && (companyMaxFields < systemMaxFields || systemMaxFields < 0)) {
    			maxFields = companyMaxFields;
    		} else {
    			maxFields = systemMaxFields;
    		}

    		List<RecipientFieldDescription> recipientFields = getRecipientFields(companyID);
    		List<RecipientFieldDescription> companySpecificFields = recipientFields.stream().filter(x -> !RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(x.getColumnName())).collect(Collectors.toList());
			int currentFieldCount = companySpecificFields.size();
			
			if (currentFieldCount < maxFields) {
				return true;
			} else if (currentFieldCount < maxFields + configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfProfileFields_Graceful)) {
				return true;
			} else {
				return false;
			}
    	}
	}
	
	@Override
	public final int countCustomerEntries(final int companyID) {
		return recipientFieldDao.countCustomerEntries(companyID);
	}

	@Override
	public boolean checkAllowedDefaultValue(int companyID, String fieldname, String fieldDefault) {
		if (getRecipientField(companyID, fieldname) != null) {
			// Field already exists, so a new default value will only take effect on newly inserted entries, which should not take too much time
			return true;
		} else {
			// Field does not exist yet, so a default value which is not empty must be copied in every existing entry, which can take a lot of time
			return StringUtils.isEmpty(fieldDefault) || recipientFieldDao.countCustomerEntries(companyID) <= configService.getIntegerValue(ConfigValue.MaximumNumberOfEntriesForDefaultValueChange, companyID);
		}
	}

	/**
	 * Get the current number of profile fields that are not included in the EMM standard fields, but created by the client for special purpose
	 */
	@Override
	public int getClientSpecificFieldCount(int companyID) {
		int companySpecificFieldCount = 0;
		
		Set<String> recipientStandardFieldColumnNames = RecipientStandardField.getAllRecipientStandardFieldColumnNames();
		
		// Collect optional column names for postal profile fields
		Set<String> postalFieldColumnNames = new HashSet<>();
		for (PostalField postalField : PostalField.values()) {
            String columnName = configService.getValue(postalField.getConfigValue(), companyID);
            if (StringUtils.isNotBlank(columnName)) {
            	postalFieldColumnNames.add(columnName);
            }
        }
		
		for (RecipientFieldDescription recipientField : getRecipientFields(companyID)) {
			if (!recipientStandardFieldColumnNames.contains(recipientField.getColumnName())
				&& !RecipientFieldService.OLD_SOCIAL_MEDIA_FIELDS.contains(recipientField.getColumnName())
				&& !postalFieldColumnNames.contains(recipientField.getColumnName())) {
					companySpecificFieldCount++;
			}
		}
		
        return companySpecificFieldCount;
	}

	@Override
	public ServiceResult<List<String>> filterAllowedForDelete(Map<String, SimpleServiceResult> validationResults, Admin admin) {
		return bulkActionValidationService.checkAllowedForDeletion(validationResults.keySet(), col -> {
			SimpleServiceResult result = validationResults.get(col);
			if (result.isSuccess()) {
				return ServiceResult.success(col);
			}
			return new ServiceResult<>(col, result.isSuccess(), result.getSuccessMessages(), result.getWarningMessages(), result.getErrorMessages());
		});
	}

	@Override
	public ServiceResult<UserAction> delete(Map<String, SimpleServiceResult> validationResults, Admin admin) {
		List<String> allowedColumns = filterAllowedForDelete(validationResults, admin).getResult();

		List<String> deletedColumns = new ArrayList<>(allowedColumns.size());
		for (String column : allowedColumns) {
			try {
				deleteRecipientField(admin.getCompanyID(), column);
				deletedColumns.add(column);
			} catch (Exception e) {
				logger.error("Cannot delete profile field: {}", column, e);
			}
		}

		return ServiceResult.success(
				new UserAction(
						"delete profile fields",
						"Profile fields: " + StringUtils.join(deletedColumns, ", ")
				),
				Message.of(Const.Mvc.SELECTION_DELETED_MSG)
		);
	}

	@Override
	public long getCountForOverview(int companyId) {
		return profileFieldDao.getCustomerColumns(companyId).stream()
				.filter(col -> !RecipientStandardField.Bounceload.getColumnName().equals(col))
				.count();
    }

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setRecipientFieldsCache(RecipientFieldsCache recipientFieldsCache) {
		this.recipientFieldsCache = recipientFieldsCache;
	}

	public void setBulkActionValidationService(BulkActionValidationService<String, String> bulkActionValidationService) {
		this.bulkActionValidationService = bulkActionValidationService;
	}

	public void setRecipientFieldDao(RecipientFieldDao recipientFieldDao) {
		this.recipientFieldDao = recipientFieldDao;
	}

	public void setProfileFieldDao(ProfileFieldDao profileFieldDao) {
		this.profileFieldDao = profileFieldDao;
	}

	public void setRecipientProfileHistoryService(RecipientProfileHistoryService recipientProfileHistoryService) {
		this.recipientProfileHistoryService = recipientProfileHistoryService;
	}
}
