/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.importvalues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.ImportStatus;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.velocity.Constants;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class ImportModeUpdateHandler implements ImportModeHandler {
    private static final transient Logger logger = LogManager.getLogger(ImportModeUpdateHandler.class);
    
    private ImportRecipientsDao importRecipientsDao;

	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}

	@Override
	public void checkPreconditions(ImportProfile importProfile) throws Exception {
		// Do nothing
	}

	@Override
	public boolean isNullValueAllowedForData(DbColumnType columnType, NullValuesAction nullValuesAction) {
		return columnType.isNullable() || nullValuesAction == NullValuesAction.IGNORE;
	}

	@Override
	public void handleNewCustomers(ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String duplicateIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Do nothing
	}
	
	@Override
	public void handlePreProcessing(EmmActionService emmActionService, ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign) throws Exception {
		// Do not remove!!!
	}

	@Override
	public void handleExistingCustomersImproved(ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String importIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Update customer data
		if (importProfile.getUpdateAllDuplicates()) {
			// Update all existing customer identified by keycolumns
			int updatedEntries = importRecipientsDao.updateAllExistingCustomersByKeyColumnImproved(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, importIndexColumn, importProfile.getNullValuesAction(), datasourceId, importProfile.getCompanyId());
			status.setUpdated(updatedEntries);
		} else {
			// Update the first existing customer only
			int updatedEntries = importRecipientsDao.updateFirstExistingCustomersImproved(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, importIndexColumn, importProfile.getNullValuesAction(), datasourceId, importProfile.getCompanyId());
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
		    			int existingCustomerSubscribed = importRecipientsDao.assignExistingCustomerWithoutBindingToMailingList(temporaryImportTableName, importProfile.getCompanyId(), mailingListId, mediatype, UserStatus.Active);
		    			
		    			if (!mailinglistAssignStatistics.containsKey(mediatype)) {
		    				mailinglistAssignStatistics.put(mediatype, new HashMap<>());
		    			}
		    			mailinglistAssignStatistics.get(mediatype).put(mailingListId, existingCustomerSubscribed);
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
