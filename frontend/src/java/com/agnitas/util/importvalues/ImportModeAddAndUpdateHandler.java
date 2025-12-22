/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.importvalues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.ImportStatus;
import com.agnitas.dao.ImportRecipientsDao;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.core.velocity.Constants;
import com.agnitas.service.ImportException;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.ImportUtils.ImportErrorType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImportModeAddAndUpdateHandler implements ImportModeHandler {

	private static final Logger logger = LogManager.getLogger(ImportModeAddAndUpdateHandler.class);
    
    protected ImportRecipientsDao importRecipientsDao;
    private ConfigService configService;
    
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}
	
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}

	@Override
	public void checkPreconditions(ImportProfile importProfile) {
		// If customer_id is imported it may only be used for updates, never for imports(ADD)
		// Permission to import customer_id is checked later
		boolean isCustomerIdImported = false;
		for (ColumnMapping mapping : importProfile.getColumnMapping()) {
			if ("customer_id".equalsIgnoreCase(mapping.getDatabaseColumn())) {
				isCustomerIdImported = true;
			}
		}
		
		if (isCustomerIdImported) {
			throw new ImportException(false, "error.import.customerid_insert");
		}
		
		// Check "not nullable" fields
		CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(importProfile.getCompanyId());
		for (Entry<String, DbColumnType> columnEntry : customerDbFields.entrySet()) {
			if (!columnEntry.getValue().isNullable()
					&& DbUtilities.getColumnDefaultValue(importRecipientsDao.getDataSource(), "customer_" + importProfile.getCompanyId() + "_tbl", columnEntry.getKey()) == null
					&& !"customer_id".equalsIgnoreCase(columnEntry.getKey())
					&& !"gender".equalsIgnoreCase(columnEntry.getKey())
					&& !"mailtype".equalsIgnoreCase(columnEntry.getKey())
					&& !RecipientStandardField.Bounceload.getColumnName().equalsIgnoreCase(columnEntry.getKey())) {
				boolean notNullColumnIsSet = false;
				for (ColumnMapping mapping : importProfile.getColumnMapping()) {
					if (columnEntry.getKey().equalsIgnoreCase(mapping.getDatabaseColumn()) && (mapping.getFileColumn() != null || mapping.getDefaultValue() != null)) {
						notNullColumnIsSet = true;
						break;
					}
				}
				if (!notNullColumnIsSet) {
					throw new ImportException(false, "error.import.missingNotNullableColumnInMapping", columnEntry.getKey());
				}
			}
		}
		
		for (ColumnMapping mapping : importProfile.getColumnMapping()) {
			if ("gender".equalsIgnoreCase(mapping.getDatabaseColumn())) {
				if (StringUtils.isBlank(mapping.getFileColumn()) && (StringUtils.isBlank(mapping.getDefaultValue()) || mapping.getDefaultValue().trim().equals("''") || mapping.getDefaultValue().trim().equalsIgnoreCase("null"))) {
					throw new ImportException(false, "error.import.missingNotNullableColumnInMapping", "gender");
				}
			} else if ("mailtype".equalsIgnoreCase(mapping.getDatabaseColumn())) {
				if (StringUtils.isBlank(mapping.getFileColumn()) && (StringUtils.isBlank(mapping.getDefaultValue()) || mapping.getDefaultValue().trim().equals("''") || mapping.getDefaultValue().trim().equalsIgnoreCase("null"))) {
					throw new ImportException(false, "error.import.missingNotNullableColumnInMapping", "mailtype");
				}
			}
		}
	}

	@Override
	public boolean isNullValueAllowedForData(DbColumnType columnType, NullValuesAction nullValuesAction) {
		return columnType.isNullable();
	}
	
	@Override
	public void handlePreProcessing(EmmActionService emmActionService, ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign) {
		// Do not remove, method is needed in agnitas trunk!!! 
	}

	@Override
	public void handleNewCustomers(ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String duplicateIndexColumn, List<String> transferDbColumns, int datasourceId) {
		// Insert customer data
	    // Check if customer limit is reached
		int numberOfCustomersLimit = this.configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfCustomers);
		// Check global license maximum first
		if (numberOfCustomersLimit < 0) {
			numberOfCustomersLimit = this.configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfCustomers, importProfile.getCompanyId());
		}
		if (numberOfCustomersLimit >= 0) {
    		int currentNumberOfCustomers = importRecipientsDao.getAllRecipientsCount(importProfile.getCompanyId());
    		int numberOfInsertableItems = importRecipientsDao.getNumberOfEntriesForInsert(temporaryImportTableName, duplicateIndexColumn);
    		int numberOfCustomersAfterImport = currentNumberOfCustomers + numberOfInsertableItems;
    		if (numberOfCustomersAfterImport > numberOfCustomersLimit) {
    			throw new ImportException(false, "error.import.maxcustomersexceeded", currentNumberOfCustomers , numberOfInsertableItems, numberOfCustomersLimit);
    		} else if (numberOfCustomersAfterImport > (numberOfCustomersLimit * 0.9)) {
    			// Shows a warning in ProfileImportAction after the import
    			status.setNearLimit(true);
			}
		}
		
		int invalidNullValueEntries = importRecipientsDao.removeNewCustomersWithInvalidNullValues(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl");
		status.setInvalidNullValues(invalidNullValueEntries);
		
		int insertedEntries = importRecipientsDao.insertNewCustomers(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, duplicateIndexColumn, datasourceId, importProfile.getDefaultMailType());
		status.setInserted(insertedEntries);
	}

	@Override
	public void handleExistingCustomersImproved(ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String importIndexColumn, List<String> transferDbColumns, int datasourceId) {
		// Update customer data
		if (importProfile.getUpdateAllDuplicates()) {
			// Update all existing customer identified by keycolumns
			int updatedEntries = importRecipientsDao.updateAllExistingCustomersByKeyColumnImproved(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, importIndexColumn, importProfile.getNullValuesAction(), datasourceId);
			status.setUpdated(updatedEntries);
		} else {
			// Update the first existing customer only
			int updatedEntries = importRecipientsDao.updateFirstExistingCustomersImproved(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, importIndexColumn, importProfile.getNullValuesAction(), datasourceId);
			status.setUpdated(updatedEntries);
		}
	}

	@Override
	public Map<MediaTypes, Map<Integer, Integer>> handlePostProcessing(EmmActionService emmActionService, ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign, Set<MediaTypes> mediatypes) throws Exception {
		if (mailingListIdsToAssign != null && mailingListIdsToAssign.size() > 0) {
			Map<MediaTypes, Map<Integer, Integer>> mailinglistAssignStatistics = new HashMap<>();
			if (importProfile.getActionForNewRecipients() > 0) {
				// Execute configured action for recipients that had no binding before this import (existing recipients without binding or completely new recipients)
				for (int mailingListId : mailingListIdsToAssign) {
					List<Integer> customerIdsForAction = importRecipientsDao.getImportedCustomerIdsWithoutBindingToMailinglist(temporaryImportTableName, importProfile.getCompanyId(), datasourceId, mailingListId);
					
		    		// Execute the action for each customer
					int succesfullyExecutedCustomerActions = 0;
					for (int customerID : customerIdsForAction) {
						try {
							final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
							
		                    Map<String, Object> params = new HashMap<>();
		                    params.put("customerID", customerID);
		                    Map<String, Object> requestParams = new HashMap<>();
		                    requestParams.put("agnSUBSCRIBE", Integer.toString(UserStatus.Active.getStatusCode()));
		                    requestParams.put("agnMAILINGLIST", Integer.toString(mailingListId));
							requestParams.put("DATASOURCE_ID", Integer.toString(datasourceId));
		                    requestParams.put("agnFN", "ProfileImport");
		                    
		                    params.put("requestParameters", requestParams);
		        			params.put(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME, actionOperationErrors);
		                    
		        			if (emmActionService != null) {
		        				emmActionService.executeActions(importProfile.getActionForNewRecipients(), importProfile.getCompanyId(), params, actionOperationErrors);
		        			}
		                    succesfullyExecutedCustomerActions++;
						} catch (Exception e) {
							logger.error("Error in actionForNewRecipients: " + e.getMessage());
							status.addError(ImportErrorType.ACTION_ERROR);
							throw e;
						}
					}
					if (!mailinglistAssignStatistics.containsKey(MediaTypes.EMAIL)) {
	    				mailinglistAssignStatistics.put(MediaTypes.EMAIL, new HashMap<>());
	    			}
		    		mailinglistAssignStatistics.get(MediaTypes.EMAIL).put(mailingListId, succesfullyExecutedCustomerActions);
				}
			} else  {
				// Insert bindings for new and existing customers (subscribe to mailinglists)
		    	for (int mailingListId : mailingListIdsToAssign) {
		    		for (MediaTypes mediatype : mediatypes) {
		    			int newCustomerSubscribed = importRecipientsDao.assignNewCustomerToMailingList(importProfile.getCompanyId(), datasourceId, mailingListId, mediatype, UserStatus.Active);
	
		    			int existingCustomerSubscribed = importRecipientsDao.assignExistingCustomerWithoutBindingToMailingList(temporaryImportTableName, importProfile.getCompanyId(), mailingListId, mediatype, UserStatus.Active);
			    		
		    			if (!mailinglistAssignStatistics.containsKey(mediatype)) {
		    				mailinglistAssignStatistics.put(mediatype, new HashMap<>());
		    			}
		    			mailinglistAssignStatistics.get(mediatype).put(mailingListId, newCustomerSubscribed + existingCustomerSubscribed);
		    		}
		    	}
			}
			return mailinglistAssignStatistics;
		} else {
			return null;
		}
	}

	@Override
	public int handleBlacklist(ImportProfile importProfile, String temporaryImportTableName) {
		return importRecipientsDao.removeBlacklistedEmails(temporaryImportTableName, importProfile.getCompanyId());
	}
}
