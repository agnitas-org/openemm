/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.servicemail.impl;

import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.servicemail.ExecutingActionFailedException;
import com.agnitas.emm.core.servicemail.SendServiceMailService;
import com.agnitas.emm.core.servicemail.UnknownActionIdException;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;
import com.agnitas.emm.core.servicemail.UnknownCustomerIdException;
import com.agnitas.emm.core.velocity.Constants;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SendServiceMailServiceImpl implements SendServiceMailService {
	
	private static final Logger logger = LogManager.getLogger(SendServiceMailServiceImpl.class);

	/** Service dealing with EMM actions. */
	private EmmActionService emmActionService;
	
	@Override
	public void sendServiceMailByEmmAction(int actionID, int customerID, int companyID) {
		if(actionID <= 0) {
			logger.error("Action ID is not positive: {}", actionID);
			throw new UnknownActionIdException(actionID);
		}
		if(customerID <= 0) {
			logger.error("Customer ID is not positive: {}", actionID);
			throw new UnknownCustomerIdException(customerID);
		}
		if(companyID <= 0) {
			logger.error("Company ID is not positive: {}", actionID);
			throw new UnknownCompanyIdException(companyID);
		}
		
		// Check, if action exists
    	if(!emmActionService.actionExists(actionID, companyID)) {
			logger.error("Action ID {} not found for company ID {}", actionID, companyID);
    		throw new UnknownActionIdException(actionID);
    	}
    	
		final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
		CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();
		params.put("customerID", customerID);
		params.put(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME, actionOperationErrors);
		
		boolean result = emmActionService.executeActions(actionID, companyID, params, actionOperationErrors);
		
		if(!result) {
			logger.error("Executing action {} failed", actionID);
			throw new ExecutingActionFailedException(actionID);
		}
		
		logger.info("Sending service mail successful (action ID {}, customer ID {}, company ID {}", actionID, customerID, companyID);
	}

	/**
	 * Set service dealing with EMM actions.
	 * 
	 * @param service service dealing with EMM actions
	 */
	public void setEmmActionService(EmmActionService service) {
		this.emmActionService = service;
	}
}
