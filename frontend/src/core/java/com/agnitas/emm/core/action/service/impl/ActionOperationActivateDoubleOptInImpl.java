/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;

import com.agnitas.beans.BindingEntry;
import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.UserStatus;
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

public class ActionOperationActivateDoubleOptInImpl implements EmmActionOperation<ActionOperationActivateDoubleOptInParameters> {

	private static final Logger logger = LogManager.getLogger(ActionOperationActivateDoubleOptInImpl.class);

	private final MailingDao mailingDao;
	private final BindingEntryDao bindingEntryDao;

    public ActionOperationActivateDoubleOptInImpl(MailingDao mailingDao, BindingEntryDao bindingEntryDao) {
        this.mailingDao = mailingDao;
        this.bindingEntryDao = bindingEntryDao;
    }

	@Override
	public boolean execute(
			ActionOperationActivateDoubleOptInParameters activateDoiOperation,
			Map<String, Object> requestParameters,
			EmmActionOperationErrors errors
	) {
		final int companyID = activateDoiOperation.getCompanyId();
		HttpServletRequest request = (HttpServletRequest) requestParameters.get("_request");

		if (requestParameters.get("customerID") == null) {
			errors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);
			logger.warn("ActivateDoubleOptIn: Missing 'customerID' parameter");
			return false;
		}

		Integer customerID;
		try {
			customerID = parseIntegerParam(requestParameters.get("customerID"));
		} catch (Exception e) {
			errors.addErrorCode(ErrorCode.INVALID_CUSTOMER_ID);
			return false;
		}

		if (customerID == 0) {
			errors.addErrorCode(ErrorCode.INVALID_CUSTOMER_ID);
			logger.warn("ActivateDoubleOptIn: Customer ID is 0");
			return false;
		}

		if (requestParameters.get("mailingID") == null) {
			errors.addErrorCode(ErrorCode.MISSING_MAILING_ID);
			logger.warn("ActivateDoubleOptIn: Missing mailing ID");
			return false;
		}

		Integer mailingID;
		try {
			mailingID = parseIntegerParam(requestParameters.get("mailingID"));
		} catch (Exception e) {
			errors.addErrorCode(ErrorCode.INVALID_MAILING_ID);
			return false;
		}

		if (mailingID <= 0) {
			errors.addErrorCode(ErrorCode.INVALID_MAILING_ID);
			return false;
		}

		if (activateDoiOperation.isForAllLists()) {
			boolean returnValue = false;
			boolean hasEmailBinding = false;
			for (BindingEntry bindingEntry : bindingEntryDao.getBindings(companyID, customerID)) {
				if (bindingEntry.getMediaType() == MediaTypes.EMAIL.getMediaCode()) {
					hasEmailBinding = true;
					returnValue |= changeBindingStatusToConfirmed(
							bindingEntry,
							companyID,
							mailingID,
							request == null ? null : request.getRemoteAddr(),
							HttpUtils.getReferrer(request)
					);
				}
			}

			if (!returnValue) {
				logger.info("ActivateDoubleOptIn: No binding status changed (company {}, customer {})", companyID, customerID);
				errors.addErrorCode(hasEmailBinding ? ErrorCode.RECIPIENT_NOT_ACTIVE : ErrorCode.BINDING_NOT_FOUND);
			}

			return returnValue;
		}

		int mailinglistID = mailingDao.getMailinglistId(mailingID, companyID);
		if (mailinglistID <= 0) {
			logger.warn("ActivateDoubleOptIn: Mailinglist ID is 0");
			errors.addErrorCode(ErrorCode.INVALID_MAILINGLIST_ID);
			return false;
		}

		BindingEntry bindingEntry = bindingEntryDao.get(customerID, companyID, mailinglistID, activateDoiOperation.getMediaType().getMediaCode());
		if (bindingEntry == null) {
			logger.info("ActivateDoubleOptIn: No binding entry found (company {}, customer {}, mailinglist {}, media: {})",
					companyID, customerID, mailinglistID,  activateDoiOperation.getMediaType().getMediaCode());
			errors.addErrorCode(ErrorCode.BINDING_NOT_FOUND);
			return false;
		}

		boolean result = changeBindingStatusToConfirmed(
				bindingEntry,
				companyID,
				mailingID,
				request == null ? null : request.getRemoteAddr(),
				HttpUtils.getReferrer(request)
		);

		if (!result) {
			logger.info("ActivateDoubleOptIn: Binding entry not changed (company {}, customer {}, mailinglist {}, media: {})",
					companyID, customerID, mailinglistID,  activateDoiOperation.getMediaType().getMediaCode());
			errors.addErrorCode(ErrorCode.RECIPIENT_NOT_ACTIVE);
		}

		return result;
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.ACTIVATE_DOUBLE_OPT_IN;
    }

    private boolean changeBindingStatusToConfirmed(
			BindingEntry bindingEntry,
			int companyID,
			int mailingID,
			String remoteAddr,
			String referrer
	) {
        switch (UserStatus.getByCode(bindingEntry.getUserStatus())) {
            case WaitForConfirm:
                bindingEntry.setUserStatus(UserStatus.Active.getStatusCode());
                bindingEntry.setUserRemark("Opt-In-IP: " + remoteAddr);
                bindingEntry.setReferrer(referrer);
                bindingEntry.setEntryMailingID(mailingID);
                bindingEntryDao.updateStatus(bindingEntry, companyID);
                return  true;

            case Active:
                return true;

            default:
                return false;
        }
	}
}
