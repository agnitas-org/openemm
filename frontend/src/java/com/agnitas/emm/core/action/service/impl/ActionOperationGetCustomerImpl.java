/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;
import java.util.Objects;

import com.agnitas.beans.Recipient;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.service.RecipientFieldService;

public class ActionOperationGetCustomerImpl implements EmmActionOperation {

//	private static final Logger logger = LogManager.getLogger(ActionOperationGetCustomerImpl.class);

	private BeanLookupFactory beanLookupFactory;
	private ConfigService configService;
	private RecipientService recipientService;
	private RecipientFieldService recipientFieldService;

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors errors) {
		
		ActionOperationGetCustomerParameters op =(ActionOperationGetCustomerParameters) operation;
        int customerID=0;
        int companyID = op.getCompanyId();
        Integer tmpNum=null;
        Recipient aCust = beanLookupFactory.getBeanRecipient();
        boolean returnValue=false;
        
        aCust.setCompanyID(companyID);
        if(params.get("customerID")!=null) {
            tmpNum=(Integer)params.get("customerID");
            customerID=tmpNum.intValue();
        }
        
        if(customerID!=0) {
            aCust.setCustomerID(customerID);
            
            if (configService.getBooleanValue(ConfigValue.UseRecipientFieldService, companyID)) {
            	aCust.setCustDBStructure(recipientFieldService.getRecipientDBStructure(companyID));
    		} else {
    			aCust.setCustDBStructure(recipientService.getRecipientDBStructure(companyID));
    		}
            
            aCust.setCustParameters(recipientService.getCustomerDataFromDb(companyID, customerID, aCust.getDateFormat()));
            aCust.setListBindings(recipientService.getMailinglistBindings(companyID, customerID));
            if(op.isLoadAlways() || aCust.isActiveSubscriber()) {
                if(!aCust.getCustParameters().isEmpty()) {
                    params.put("customerData", aCust.getCustParameters());
                    params.put("customerBindings", aCust.getListBindings());
                    returnValue=true;
                } else {
                	errors.addErrorCode(ErrorCode.NO_RECIPIENT_PROFILE_FIELDS_READ);
                }
            } else {
            	errors.addErrorCode(ErrorCode.RECIPIENT_NOT_ACTIVE);
            }
        } else {
        	errors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);
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

    public void setRecipientService(RecipientService recipientService) {
        this.recipientService = recipientService;
    }
	
	public void setRecipientFieldService(RecipientFieldService recipientFieldService) {
		this.recipientFieldService = Objects.requireNonNull(recipientFieldService, "RecipientField Service cannot be null");
	}
	
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
}
