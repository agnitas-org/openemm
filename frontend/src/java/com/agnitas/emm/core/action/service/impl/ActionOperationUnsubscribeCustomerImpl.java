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
import org.agnitas.beans.Mailing;
import org.agnitas.beans.Recipient;
import org.agnitas.dao.MailingDao;
import org.agnitas.dao.UserStatus;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUnsubscribeCustomerParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class ActionOperationUnsubscribeCustomerImpl implements EmmActionOperation {

	/** DAO accessing mailing data. */
	private MailingDao mailingDao;

	private BeanLookupFactory beanLookupFactory;
	
	private ActionOperationUnsubscribeCustomerImpl() { /* Nothing to do here. TODO: Remove??? */ }

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) throws Exception {
		ActionOperationUnsubscribeCustomerParameters op =(ActionOperationUnsubscribeCustomerParameters) operation;
		int companyID = op.getCompanyId();

        int customerID = 0;
        int mailingID = 0;
        Recipient aCust = beanLookupFactory.getBeanRecipient();
        
        aCust.setCompanyID(companyID);
        if (params.get("customerID") != null) {
            customerID = ((Integer) params.get("customerID")).intValue();
        }
        
        if (params.get("mailingID") != null) {
            mailingID = ((Integer) params.get("mailingID")).intValue();
        }
        
        if (customerID != 0 && mailingID != 0) {
            aCust.setCustomerID(customerID);
            aCust.loadCustDBStructure();
            aCust.loadAllListBindings();

            Mailing aMailing = mailingDao.getMailing(mailingID, companyID);

            int mailinglistID = aMailing.getMailinglistID();
            Map<Integer, Map<Integer, BindingEntry>> aTbl = aCust.getListBindings();

            if (aTbl.containsKey(mailinglistID)) {
            	Map<Integer, BindingEntry> aTbl2 = aTbl.get(mailinglistID);
                if (aTbl2.containsKey(MediaTypes.EMAIL.getMediaCode())) {
                    BindingEntry aEntry = aTbl2.get(MediaTypes.EMAIL.getMediaCode());
                    switch(UserStatus.getUserStatusByID(aEntry.getUserStatus())) {
                        case Active:
                        case Bounce:
                        case Suspend:
                            if (!aEntry.getUserType().equals(UserType.TestVIP.getTypeCode()) && !aEntry.getUserType().equals(UserType.WorldVIP.getTypeCode())) {
                                aEntry.setUserStatus(UserStatus.UserOut.getStatusCode());
                                aEntry.setUserRemark("Opt-Out-Mailing: " + mailingID);
                                aEntry.setExitMailingID(mailingID);
                                aEntry.updateBindingInDB(companyID);
                                params.put("__agn_USER_STATUS", "4"); // next Event-Mailing goes to a user with status 4
                            }
                            return true;
                            
                        case AdminOut:
                        case UserOut:
                        	params.put("__agn_USER_STATUS", "4"); // next Event-Mailing goes to a user with status 4
                        	return true;
                            
                        default:
                            return false;
                    }
                }
            }
        }
        return false;
	}

	/**
	 * Set DAO accessing mailing data.
	 * 
	 * @param mailingDao DAO accessing mailing data
	 */
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	public void setBeanLookupFactory(BeanLookupFactory beanLookupFactory) {
		this.beanLookupFactory = beanLookupFactory;
	}
}
