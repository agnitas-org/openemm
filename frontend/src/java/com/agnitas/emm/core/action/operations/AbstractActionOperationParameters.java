/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ActionOperationActivateDoubleOptInParameters.class, name = "ActivateDoubleOptIn"),
    @JsonSubTypes.Type(value = ActionOperationContentViewParameters.class, name = "ContentView"),
    @JsonSubTypes.Type(value = ActionOperationExecuteScriptParameters.class, name = "ExecuteScript"),
    @JsonSubTypes.Type(value = ActionOperationGetArchiveListParameters.class, name = "GetArchiveList"),
    @JsonSubTypes.Type(value = ActionOperationGetArchiveMailingParameters.class, name = "GetArchiveMailing"),
    @JsonSubTypes.Type(value = ActionOperationGetCustomerParameters.class, name = "GetCustomer"),
    @JsonSubTypes.Type(value = ActionOperationIdentifyCustomerParameters.class, name = "IdentifyCustomer"),
    @JsonSubTypes.Type(value = ActionOperationSendMailingParameters.class, name = "SendMailing"),
    @JsonSubTypes.Type(value = ActionOperationServiceMailParameters.class, name = "ServiceMail"),
    @JsonSubTypes.Type(value = ActionOperationSubscribeCustomerParameters.class, name = "SubscribeCustomer"),
    @JsonSubTypes.Type(value = ActionOperationUnsubscribeCustomerParameters.class, name = "UnsubscribeCustomer"),
    @JsonSubTypes.Type(value = ActionOperationUpdateCustomerParameters.class, name = "UpdateCustomer")
})
public abstract class AbstractActionOperationParameters implements ActionOperationParameters {
	private int id;
	private int companyId;
	private int actionId;
	private final ActionOperationType type;

	public AbstractActionOperationParameters(ActionOperationType type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	public ActionOperationType getOperationType() {
		return this.type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractActionOperationParameters other = (AbstractActionOperationParameters) obj;
		if (id == 0) {
			return false;
		}
		return id == other.id;
	}

	@Override
	public int hashCode() {
		return id;
	}
}
