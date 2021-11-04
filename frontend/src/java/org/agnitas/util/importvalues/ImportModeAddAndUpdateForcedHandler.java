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
import java.util.Set;

import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.ImportStatus;
import org.agnitas.dao.UserStatus;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class ImportModeAddAndUpdateForcedHandler extends ImportModeAddAndUpdateHandler {
    private static final transient Logger logger = Logger.getLogger(ImportModeAddAndUpdateForcedHandler.class);
    
	@Override
	public Map<Integer, Integer> handlePostProcessing(EmmActionService emmActionService, ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign, Set<MediaTypes> mediatypes) throws Exception {
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
		                    Map<String, Object> params = new HashMap<>();
		                    params.put("customerID", customerID);
		                    
		        			final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
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
		    		int newCustomerSubscribed = 0;
		    		int existingCustomerSubscribed = 0;
		    		for (MediaTypes mediatype : mediatypes) {
			    		newCustomerSubscribed += importRecipientsDao.assignNewCustomerToMailingList(importProfile.getCompanyId(), datasourceId, mailingListId, mediatype, UserStatus.Active);
			    		
			    		existingCustomerSubscribed += importRecipientsDao.assignExistingCustomerWithoutBindingToMailingList(temporaryImportTableName, importProfile.getCompanyId(), mailingListId, mediatype, UserStatus.Active);
	
		    			existingCustomerSubscribed += importRecipientsDao.changeStatusInMailingList(temporaryImportTableName, importProfile.getKeyColumns(), importProfile.getCompanyId(), mailingListId, mediatype, UserStatus.UserOut.getStatusCode(), UserStatus.Active.getStatusCode(), "RecipientImport");
		    			existingCustomerSubscribed += importRecipientsDao.changeStatusInMailingList(temporaryImportTableName, importProfile.getKeyColumns(), importProfile.getCompanyId(), mailingListId, mediatype, UserStatus.AdminOut.getStatusCode(), UserStatus.Active.getStatusCode(), "RecipientImport");
		    		}
		    		
		    		mailinglistAssignStatistics.put(mailingListId, newCustomerSubscribed + existingCustomerSubscribed);
		    	}
			}
			return mailinglistAssignStatistics;
		} else {
			return null;
		}
	}
}
