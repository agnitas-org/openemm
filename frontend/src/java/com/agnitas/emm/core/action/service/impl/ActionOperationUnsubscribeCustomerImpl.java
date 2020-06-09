/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.Recipient;
import org.agnitas.dao.UserStatus;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUnsubscribeCustomerParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class ActionOperationUnsubscribeCustomerImpl implements EmmActionOperation {
	/**
	 * DAO for accessing mailing data.
	 */
	private ComMailingDao mailingDao;

	private BeanLookupFactory beanLookupFactory;

	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setBeanLookupFactory(BeanLookupFactory beanLookupFactory) {
		this.beanLookupFactory = beanLookupFactory;
	}

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) throws Exception {
		ActionOperationUnsubscribeCustomerParameters op = (ActionOperationUnsubscribeCustomerParameters) operation;
		int companyID = op.getCompanyId();

		int customerID = 0;
		int mailingID = 0;
		if (params.get("customerID") != null) {
			customerID = ((Integer) params.get("customerID")).intValue();
		}

		if (params.get("mailingID") != null) {
			mailingID = ((Integer) params.get("mailingID")).intValue();
		}

		if (customerID != 0 && mailingID != 0) {
			Recipient aCust = beanLookupFactory.getBeanRecipient();
			aCust.setCompanyID(companyID);
			aCust.setCustomerID(customerID);
			aCust.loadCustDBStructure();
			aCust.loadAllListBindings();

			final int mailinglistID = mailingDao.getMailinglistId(mailingID, companyID);
			if (mailinglistID <= 0) {
				return false;
			} else {
				Map<Integer, Map<Integer, BindingEntry>> bindingsByMailinglistAndMediatype = aCust.getListBindings();
				if (bindingsByMailinglistAndMediatype.containsKey(mailinglistID)) {
					Map<Integer, BindingEntry> bindingsByMediatype = bindingsByMailinglistAndMediatype.get(mailinglistID);
					if (bindingsByMediatype.containsKey(MediaTypes.EMAIL.getMediaCode())) {
						BindingEntry aEntry = bindingsByMediatype.get(MediaTypes.EMAIL.getMediaCode());
						switch (UserStatus.getUserStatusByID(aEntry.getUserStatus())) {
							case Active:
							case Bounce:
							case Suspend:
								if (!aEntry.getUserType().equals(UserType.TestVIP.getTypeCode()) && !aEntry.getUserType().equals(UserType.WorldVIP.getTypeCode())) {
									aEntry.setUserStatus(UserStatus.UserOut.getStatusCode());
									aEntry.setUserRemark("Opt-Out-Mailing: " + mailingID);
									aEntry.setExitMailingID(mailingID);
									aEntry.updateBindingInDB(companyID);
									// next Event-Mailing goes to a user with status 4
									params.put("__agn_USER_STATUS", "4");
								}
								return true;
		
							case AdminOut:
							case UserOut:
								// next Event-Mailing goes to a user with status 4
								params.put("__agn_USER_STATUS", "4");
								return true;
		
							default:
								return false;
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}
}
