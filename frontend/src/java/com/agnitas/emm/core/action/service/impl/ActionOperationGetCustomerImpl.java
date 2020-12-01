/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;

import org.agnitas.beans.Recipient;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;

public class ActionOperationGetCustomerImpl implements EmmActionOperation {

//	private static final Logger logger = Logger.getLogger(ActionOperationGetCustomerImpl.class);

	private BeanLookupFactory beanLookupFactory;

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors errors) {
		
		ActionOperationGetCustomerParameters op =(ActionOperationGetCustomerParameters) operation;
        int customerID=0;
        Integer tmpNum=null;
        Recipient aCust = beanLookupFactory.getBeanRecipient();
        boolean returnValue=false;
        
        aCust.setCompanyID(op.getCompanyId());
        if(params.get("customerID")!=null) {
            tmpNum=(Integer)params.get("customerID");
            customerID=tmpNum.intValue();
        }
        
        if(customerID!=0) {
            aCust.setCustomerID(customerID);
            aCust.loadCustDBStructure();
            aCust.getCustomerDataFromDb();
            aCust.loadAllListBindings();
            if(op.isLoadAlways() || aCust.isActiveSubscriber()) {
                if(!aCust.getCustParameters().isEmpty()) {
                    params.put("customerData", aCust.getCustParameters());
                    params.put("customerBindings", aCust.getListBindings());
                    returnValue=true;
                }
            }
        }
        
        return returnValue;
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.GET_CUSTOMER;
    }

    public void setBeanLookupFactory(BeanLookupFactory beanLookupFactory) {
		this.beanLookupFactory = beanLookupFactory;
	}

}
