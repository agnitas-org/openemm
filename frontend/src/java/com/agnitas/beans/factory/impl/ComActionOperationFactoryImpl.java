/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.factory.impl;

import org.agnitas.beans.factory.ActionOperationFactory;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationActivateDoubleOptInParameters;
import com.agnitas.emm.core.action.operations.ActionOperationContentViewParameters;
import com.agnitas.emm.core.action.operations.ActionOperationExecuteScriptParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveListParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationIdentifyCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationServiceMailParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUnsubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;

public class ComActionOperationFactoryImpl implements ActionOperationFactory {
	
	// TODO Replace this constants by ActionOperationParameters.ActionOperationType
	private static final String ACTIVATE_DOUBLE_OPT_IN = "ActivateDoubleOptIn";
	private static final String CONTENT_VIEW = "ContentView";
	private static final String EXECUTE_SCRIPT = "ExecuteScript";
	private static final String GET_ARCHIVE_MAILING = "GetArchiveMailing";
	private static final String GET_CUSTOMER = "GetCustomer";
	private static final String SEND_MAILING = "SendMailing";
	private static final String SERVICE_MAIL = "ServiceMail";
	private static final String UNSUBSCRIBE_CUSTOMER = "UnsubscribeCustomer";
	private static final String UPDATE_CUSTOMER = "UpdateCustomer";
	private static final String GET_ARCHIVE_LIST = "GetArchiveList";
	private static final String IDENTIFY_CUSTOMER = "IdentifyCustomer";
	private static final String SUBSCRIBE_CUSTOMER = "SubscribeCustomer";

	// TODO Replace this constants by ActionOperationParameters.ActionOperationType.values()
	private final String[] types = new String[] { 
			ACTIVATE_DOUBLE_OPT_IN,
	        CONTENT_VIEW,
	        EXECUTE_SCRIPT,
	        GET_ARCHIVE_MAILING,
	        GET_CUSTOMER,
	        SEND_MAILING,
	        SERVICE_MAIL,
	        UNSUBSCRIBE_CUSTOMER,
	        UPDATE_CUSTOMER,
	        GET_ARCHIVE_LIST,
	        IDENTIFY_CUSTOMER,
	        SUBSCRIBE_CUSTOMER
			};
	
    @Override
	public AbstractActionOperationParameters newActionOperation(String type) { // TODO: Use ActionOperationParameters.ActionOperationType instead of String here
    	// TODO Convert to switch-case-statement
        if (type.equals(ACTIVATE_DOUBLE_OPT_IN)) return new ActionOperationActivateDoubleOptInParameters();
        if (type.equals(CONTENT_VIEW)) 		     return new ActionOperationContentViewParameters();
        if (type.equals(EXECUTE_SCRIPT))         return new ActionOperationExecuteScriptParameters();
        if (type.equals(GET_ARCHIVE_MAILING))    return new ActionOperationGetArchiveMailingParameters();
        if (type.equals(GET_CUSTOMER))           return new ActionOperationGetCustomerParameters();
        if (type.equals(SEND_MAILING))           return new ActionOperationSendMailingParameters();
        if (type.equals(SERVICE_MAIL))           return new ActionOperationServiceMailParameters();
        if (type.equals(UNSUBSCRIBE_CUSTOMER))   return new ActionOperationUnsubscribeCustomerParameters();
        if (type.equals(UPDATE_CUSTOMER))        return new ActionOperationUpdateCustomerParameters();
        if (type.equals(GET_ARCHIVE_LIST))       return new ActionOperationGetArchiveListParameters();
        if (type.equals(IDENTIFY_CUSTOMER))      return new ActionOperationIdentifyCustomerParameters();
        if (type.equals(SUBSCRIBE_CUSTOMER))     return new ActionOperationSubscribeCustomerParameters();
        throw new RuntimeException("Unsupported type");
    }

	@Override
	public String[] getTypes() {
		return types;
	}
	
	@Override
	public String getType(ActionOperationParameters actionOperation) {
		if (actionOperation instanceof AbstractActionOperationParameters) {
			return ((AbstractActionOperationParameters) actionOperation).getOperationType().getName();
		}

		throw new RuntimeException("Unsupported type");
	}

}
