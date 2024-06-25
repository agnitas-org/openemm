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

import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.ImportStatus;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.velocity.Constants;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

/**
 * Import mode "import.mode.add_update_forced"
 * 	English text: "Add new recipients and update existing recipients. Subscribe unsubscribers.
 * 	German text: "Neue Empfänger hinzufügen und bestehende aktualisieren. Abmelder auch anmelden.
 * 
 * In this import mode, the following recipients are subscribed to the defined mailinglists:
 * 	- New recipients who do not have a binding for these mailinglists
 * 	- Existing recipients who do not have a binding for these mailinglists
 * 	- Existing recipients that have been unsubscribed by an admin
 * 	- Existing recipients that have been unsubscribed by the recipient himself
 * 
 * NO bounced recipients!
 */
public class ImportModeAddAndUpdateForcedHandler extends ImportModeAddAndUpdateHandler {
    private static final transient Logger logger = LogManager.getLogger(ImportModeAddAndUpdateForcedHandler.class);
    
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
		                    Map<String, Object> params = new HashMap<>();
		                    params.put("customerID", customerID);
		                    
		        			final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
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
		    			int newCustomerSubscribed = importRecipientsDao.assignNewCustomerToMailingList(importProfile.getCompanyId(), datasourceId, mailingListId, mediatype, UserStatus.Active);
			    		
		    			int existingCustomerSubscribed = importRecipientsDao.assignExistingCustomerWithoutBindingToMailingList(temporaryImportTableName, importProfile.getCompanyId(), mailingListId, mediatype, UserStatus.Active);
	
		    			existingCustomerSubscribed += importRecipientsDao.changeStatusInMailingList(temporaryImportTableName, importProfile.getKeyColumns(), importProfile.getCompanyId(), mailingListId, mediatype, UserStatus.UserOut.getStatusCode(), UserStatus.Active.getStatusCode(), "RecipientImport");
		    			existingCustomerSubscribed += importRecipientsDao.changeStatusInMailingList(temporaryImportTableName, importProfile.getKeyColumns(), importProfile.getCompanyId(), mailingListId, mediatype, UserStatus.AdminOut.getStatusCode(), UserStatus.Active.getStatusCode(), "RecipientImport");
		    			
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
}
