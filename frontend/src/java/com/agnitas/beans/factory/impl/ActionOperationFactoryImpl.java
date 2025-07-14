/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.factory.impl;

import java.util.Arrays;
import java.util.List;

import com.agnitas.beans.factory.ActionOperationFactory;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationActivateDoubleOptInParameters;
import com.agnitas.emm.core.action.operations.ActionOperationContentViewParameters;
import com.agnitas.emm.core.action.operations.ActionOperationExecuteScriptParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveListParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationIdentifyCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendLastNewsletterParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationServiceMailParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.operations.ActionOperationUnsubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;

public class ActionOperationFactoryImpl implements ActionOperationFactory {
	
    @Override
	public AbstractActionOperationParameters newActionOperation(String typeName) {
        return newActionOperation(ActionOperationType.fromTypeName(typeName));
    }

    @Override
	public AbstractActionOperationParameters newActionOperation(ActionOperationType type) {
		if (type == null) {
    		throw new RuntimeException("Unsupported type");
		}

		switch (type) {
			case ACTIVATE_DOUBLE_OPT_IN:
				return new ActionOperationActivateDoubleOptInParameters();
			case CONTENT_VIEW:
				return new ActionOperationContentViewParameters();
			case EXECUTE_SCRIPT:
				return new ActionOperationExecuteScriptParameters();
			case GET_ARCHIVE_MAILING:
				return new ActionOperationGetArchiveMailingParameters();
			case GET_CUSTOMER:
				return new ActionOperationGetCustomerParameters();
			case SEND_MAILING:
				return new ActionOperationSendMailingParameters();
			case SERVICE_MAIL:
				return new ActionOperationServiceMailParameters();
			case UNSUBSCRIBE_CUSTOMER:
				return new ActionOperationUnsubscribeCustomerParameters();
			case UPDATE_CUSTOMER:
				return new ActionOperationUpdateCustomerParameters();
			case GET_ARCHIVE_LIST:
				return new ActionOperationGetArchiveListParameters();
			case IDENTIFY_CUSTOMER:
				return new ActionOperationIdentifyCustomerParameters();
			case SUBSCRIBE_CUSTOMER:
				return new ActionOperationSubscribeCustomerParameters();
			case SEND_LAST_NEWSLETTER:
				return new ActionOperationSendLastNewsletterParameters();
				
			default:
				throw new RuntimeException("Unsupported type");
		}
    }

    @Override
	public List<ActionOperationType> getTypesList() {
		return Arrays.asList(ActionOperationType.values());
	}

}
