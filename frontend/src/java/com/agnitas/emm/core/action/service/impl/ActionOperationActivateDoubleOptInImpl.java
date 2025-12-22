/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.List;
import java.util.Map;

import com.agnitas.beans.BindingEntry;
import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationActivateDoubleOptInParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionOperationActivateDoubleOptInImpl implements EmmActionOperation {

	private static final Logger logger = LogManager.getLogger(ActionOperationActivateDoubleOptInImpl.class);

	private MailingDao mailingDao;
	private BindingEntryDao bindingEntryDao;

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	public void setBindingEntryDao(BindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
	}

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> requestParameters, EmmActionOperationErrors errors) {
		final ActionOperationActivateDoubleOptInParameters activateDoiOperation = (ActionOperationActivateDoubleOptInParameters) operation;
		
		assert activateDoiOperation.getMediaType() != null;
		
		final int companyID = activateDoiOperation.getCompanyId();
		HttpServletRequest request = (HttpServletRequest) requestParameters.get("_request");

		if (requestParameters.get("customerID") == null) {
			errors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);
			
			logger.warn("ActivateDoubleOptIn: Missing 'customerID' parameter");
			
			return false;
		} else {
			int customerID = (Integer) requestParameters.get("customerID");
			if (customerID == 0) {
				errors.addErrorCode(ErrorCode.INVALID_CUSTOMER_ID);
				
				logger.warn("ActivateDoubleOptIn: Customer ID is 0");
				
				return false;
			} else {
				if (activateDoiOperation.isForAllLists()) {
					List<BindingEntry> bindingEntries = bindingEntryDao.getBindings(companyID, customerID);
					boolean returnValue = false;
					for (BindingEntry bindingEntry : bindingEntries) {
						if (bindingEntry.getMediaType() == MediaTypes.EMAIL.getMediaCode()) {
							int mailingID = 0;
							try {
								mailingID = (Integer) requestParameters.get("mailingID");
							} catch (Exception e) {
								errors.addErrorCode(ErrorCode.INVALID_MAILING_ID);
								
								logger.warn("ActivateDoubleOptIn: Invalid mailingID");
							}
							returnValue |= changeBindingStatusToConfirmed(bindingEntry, companyID, mailingID, request.getRemoteAddr(), HttpUtils.getReferrer(request));
						}
					}
					
					if(!returnValue) {
						logger.info("ActivateDoubleOptIn: No binding status changed (company {}, customer {})", companyID, customerID);
					}
					
					return returnValue;
				} else {
					if (requestParameters.get("mailingID") == null) {
						errors.addErrorCode(ErrorCode.MISSING_MAILING_ID);
						
						logger.warn("ActivateDoubleOptIn: Missing mailing ID");
						
						return false;
					} else {
						int mailingID = (Integer) requestParameters.get("mailingID");
						int mailinglistID = mailingDao.getMailinglistId(mailingID, companyID);
						if (mailinglistID <= 0) {
							logger.warn("ActivateDoubleOptIn: Mailinglist ID is 0");
							
							errors.addErrorCode(ErrorCode.INVALID_MAILINGLIST_ID);
							
							return false;
						} else {
							final BindingEntry bindingEntry = bindingEntryDao.get(customerID, companyID, mailinglistID, activateDoiOperation.getMediaType().getMediaCode());
							if (bindingEntry != null) {
								final boolean result = changeBindingStatusToConfirmed(bindingEntry, companyID, mailingID, request.getRemoteAddr(), HttpUtils.getReferrer(request));

								if(!result) {
									logger.info("ActivateDoubleOptIn: Binding entry not changed (company {}, customer {}, mailinglist {}, media: {})", companyID, customerID, mailinglistID,  activateDoiOperation.getMediaType().getMediaCode());
								}
								
								return result;
							} else {
								logger.info("ActivateDoubleOptIn: No binding entry found (company {}, customer {}, mailinglist {}, media: {})", companyID, customerID, mailinglistID,  activateDoiOperation.getMediaType().getMediaCode());
								return false;
							}
						}
					}
				}
			}
		}
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.ACTIVATE_DOUBLE_OPT_IN;
    }

    private boolean changeBindingStatusToConfirmed(BindingEntry aEntry, int companyID, int mailingID, String remoteAddr, String referrer) {
        switch (UserStatus.getByCode(aEntry.getUserStatus())) {
            case WaitForConfirm:
                aEntry.setUserStatus(UserStatus.Active.getStatusCode());
                aEntry.setUserRemark("Opt-In-IP: " + remoteAddr);
                aEntry.setReferrer(referrer);
                aEntry.setEntryMailingID(mailingID);
                bindingEntryDao.updateStatus(aEntry, companyID);
                return  true;
                
            case Active:
                return true;
                
            default:
                return false;
        }
	}
}
