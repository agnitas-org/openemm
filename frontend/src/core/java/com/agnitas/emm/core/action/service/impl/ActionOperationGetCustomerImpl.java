/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.beans.Recipient;
import com.agnitas.emm.core.action.operations.ActionOperationGetCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.service.RecipientFieldService;

public class ActionOperationGetCustomerImpl implements EmmActionOperation<ActionOperationGetCustomerParameters> {

	private final BeanLookupFactory beanLookupFactory;
	private final ConfigService configService;
	private final RecipientService recipientService;
	private final RecipientFieldService recipientFieldService;

	public ActionOperationGetCustomerImpl(
			BeanLookupFactory beanLookupFactory,
			ConfigService configService,
			RecipientService recipientService,
			RecipientFieldService recipientFieldService
	) {
		this.beanLookupFactory = beanLookupFactory;
		this.configService = configService;
		this.recipientService = recipientService;
		this.recipientFieldService = recipientFieldService;
	}

	@Override
	public boolean execute(
            ActionOperationGetCustomerParameters op,
            Map<String, Object> params,
            EmmActionOperationErrors errors
    ) {
        if (!params.containsKey("customerID")) {
            errors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);
            return false;
        }

        Integer customerID;
        try {
            customerID = parseIntegerParam(params.get("customerID"));
        } catch (Exception e) {
            errors.addErrorCode(ErrorCode.INVALID_CUSTOMER_ID);
            return false;
        }

        if (customerID != null && customerID <= 0) {
            errors.addErrorCode(ErrorCode.INVALID_CUSTOMER_ID);
            return false;
        }

        int companyID = op.getCompanyId();

        Recipient recipient = beanLookupFactory.getBeanRecipient();
        recipient.setCompanyID(companyID);
        recipient.setCustomerID(customerID);

        if (configService.getBooleanValue(ConfigValue.UseRecipientFieldService, companyID)) {
            recipient.setCustDBStructure(recipientFieldService.getRecipientDBStructure(companyID));
        } else {
            recipient.setCustDBStructure(recipientService.getRecipientDBStructure(companyID));
        }

        recipient.setCustParameters(recipientService.getCustomerDataFromDb(companyID, customerID, recipient.getDateFormat()));
        recipient.setListBindings(recipientService.getMailinglistBindings(companyID, customerID));
        if(op.isLoadAlways() || recipient.isActiveSubscriber()) {
            if(!recipient.getCustParameters().isEmpty()) {
                params.put("customerData", recipient.getCustParameters());
                params.put("customerBindings", recipient.getListBindings());
                return true;
            }

            errors.addErrorCode(ErrorCode.NO_RECIPIENT_PROFILE_FIELDS_READ);
        } else {
            errors.addErrorCode(ErrorCode.RECIPIENT_NOT_ACTIVE);
        }

        return false;
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.GET_CUSTOMER;
    }

}
