/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.Mailing;
import org.agnitas.dao.MailingDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.util.HttpUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationActivateDoubleOptInParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class ActionOperationActivateDoubleOptInImpl implements EmmActionOperation {
	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ActionOperationActivateDoubleOptInImpl.class);

	/** DAO accessing mailing data. */
	private MailingDao mailingDao;
	
	private ComBindingEntryDao bindingEntryDao;

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> requestParameters, final EmmActionOperationErrors errors) throws Exception {
		final ActionOperationActivateDoubleOptInParameters activateDoiOperation = (ActionOperationActivateDoubleOptInParameters) operation;
		
		assert activateDoiOperation.getMediaType() != null;
		
		final int companyID = activateDoiOperation.getCompanyId();
		HttpServletRequest request = (HttpServletRequest) requestParameters.get("_request");

		if (requestParameters.get("customerID") == null) {
			return false;
		} else {
			int customerID = (Integer) requestParameters.get("customerID");
			if (customerID == 0) {
				return false;
			} else {
				if (activateDoiOperation.isForAllLists()) {
					List<BindingEntry> bindingEntries = bindingEntryDao.getBindings(companyID, customerID);
					boolean returnValue = false;
					for (BindingEntry bindingEntry : bindingEntries) {
						if (bindingEntry.getMediaType() == MediaTypes.EMAIL.getMediaCode()) {
							returnValue |= changeBindingStatusToConfirmed(bindingEntry, companyID, request.getRemoteAddr(), HttpUtils.getReferrer(request));
						}
					}
					return returnValue;
				} else {
					if (requestParameters.get("mailingID") == null) {
						return false;
					} else {
						int mailingID = (Integer) requestParameters.get("mailingID");
						Mailing mailing = mailingDao.getMailing(mailingID, companyID);
						if (mailing == null) {
							return false;
						} else {
							BindingEntry bindingEntry = bindingEntryDao.get(customerID, companyID, mailing.getMailinglistID(), activateDoiOperation.getMediaType().getMediaCode());
							if (bindingEntry != null) {
								return changeBindingStatusToConfirmed(bindingEntry, companyID, request.getRemoteAddr(), HttpUtils.getReferrer(request));
							} else {
								return false;
							}
						}
					}
				}
			}
		}
	}
	
	private boolean changeBindingStatusToConfirmed(BindingEntry aEntry, int companyID, String remoteAddr, String referrer) throws Exception {
        switch (UserStatus.getUserStatusByID(aEntry.getUserStatus())) {
            case WaitForConfirm:
                aEntry.setUserStatus(UserStatus.Active.getStatusCode());
                aEntry.setUserRemark("Opt-In-IP: " + remoteAddr);
                aEntry.setReferrer(referrer);
                bindingEntryDao.updateStatus(aEntry, companyID);
                return  true;
                
            case Active:
                return true;
                
            default:
                return false;
        }
	}

	/**
	 * Set DAO accessing mailing data.
	 * 
	 * @param mailingDao DAO accessing mailing data
	 */
	@Required
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	@Required
	public void setBindingEntryDao(ComBindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
	}
}
