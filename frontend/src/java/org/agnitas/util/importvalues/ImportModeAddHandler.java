/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.importvalues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.CustomerImportStatus;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.ImportException;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;

public class ImportModeAddHandler implements ImportModeHandler {
    private static final transient Logger logger = Logger.getLogger(ImportModeAddHandler.class);
    
    private ImportRecipientsDao importRecipientsDao;
    
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}

	@Override
	public void checkPreconditions(ImportProfile importProfile) throws Exception {
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
		
		// Check not-nullable fields
		CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(importProfile.getCompanyId());
		for (Entry<String, DbColumnType> columnEntry : customerDbFields.entrySet()) {
			if (!columnEntry.getValue().isNullable()
					&& DbUtilities.getColumnDefaultValue(importRecipientsDao.getDataSource(), "customer_" + importProfile.getCompanyId() + "_tbl", columnEntry.getKey()) == null
					&& !"customer_id".equalsIgnoreCase(columnEntry.getKey())
					&& !"gender".equalsIgnoreCase(columnEntry.getKey())
					&& !"mailtype".equalsIgnoreCase(columnEntry.getKey())
					&& !ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD.equalsIgnoreCase(columnEntry.getKey())) {
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
	public void handleNewCustomers(CustomerImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String duplicateIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Insert customer data
		// Check if customer limit is reached
		int numberOfCustomersLimit = ConfigService.getInstance().getIntegerValue(ConfigValue.System_License_MaximumNumberOfCustomers);
		// Check global license maximum first
		if (numberOfCustomersLimit < 0) {
			numberOfCustomersLimit = ConfigService.getInstance().getIntegerValue(ConfigValue.System_License_MaximumNumberOfCustomers, importProfile.getCompanyId());
		}
		if (numberOfCustomersLimit >= 0) {
    		int currentNumberOfCustomers = DbUtilities.getTableEntriesCount(importRecipientsDao.getDataSource(), "customer_" + importProfile.getCompanyId() + "_tbl");
    		int numberOfInsertableItems = importRecipientsDao.getNumberOfEntriesForInsert(temporaryImportTableName, duplicateIndexColumn);
    		int numberOfCustomersAfterImport = currentNumberOfCustomers + numberOfInsertableItems;
    		if (numberOfCustomersAfterImport > numberOfCustomersLimit) {
    			throw new ImportException(false, "error.import.maxcustomersexceeded", currentNumberOfCustomers , numberOfInsertableItems, numberOfCustomersLimit);
    		} else if (numberOfCustomersAfterImport > (numberOfCustomersLimit * 0.9)) {
    			// Shows a warning in ProfileImportAction after the import
    			status.setNearLimit(true);
			}
		}
		
		int invalidNullValueEntries = importRecipientsDao.removeNewCustomersWithInvalidNullValues(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, duplicateIndexColumn, importProfile.getColumnMapping());
		status.setInvalidNullValues(invalidNullValueEntries);
		
		int insertedEntries = importRecipientsDao.insertNewCustomers(temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, duplicateIndexColumn, datasourceId, importProfile.getDefaultMailType(), importProfile.getColumnMapping());
		status.setInserted(insertedEntries);
	}

	@Override
	public void handleExistingCustomers(CustomerImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String importIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Do nothing
	}

	@Override
	public Map<Integer, Integer> handlePostProcessing(EmmActionService emmActionService, CustomerImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign) throws Exception {
		if (mailingListIdsToAssign != null && mailingListIdsToAssign.size() > 0) {
			Map<Integer, Integer> mailinglistAssignStatistics = new HashMap<>();
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
		                    requestParams.put("agnFN", "ProfileImport");
		                    
		                    params.put("requestParameters", requestParams);
		        			params.put("actionErrors", actionOperationErrors);
		        			
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
		    		mailinglistAssignStatistics.put(mailingListId, succesfullyExecutedCustomerActions);
				}
			} else  {
				// Insert bindings for new and existing customers (subscribe to mailinglists)
		    	for (int mailingListId : mailingListIdsToAssign) {
		    		int newCustomerSubscribed = importRecipientsDao.assignNewCustomerToMailingList(importProfile.getCompanyId(), datasourceId, mailingListId, UserStatus.Active);
		    		
		    		mailinglistAssignStatistics.put(mailingListId, newCustomerSubscribed);
		    	}
			}
			return mailinglistAssignStatistics;
		} else {
			return null;
		}
	}
}
