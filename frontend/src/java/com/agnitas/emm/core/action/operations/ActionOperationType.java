/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

public enum ActionOperationType {
	ACTIVATE_DOUBLE_OPT_IN("ActivateDoubleOptIn"),
	CONTENT_VIEW("ContentView"),
	EXECUTE_SCRIPT("ExecuteScript"),
	GET_ARCHIVE_LIST("GetArchiveList"),
	GET_ARCHIVE_MAILING("GetArchiveMailing"),
	GET_CUSTOMER("GetCustomer"),
	IDENTIFY_CUSTOMER("IdentifyCustomer"),
	SEND_LAST_NEWSLETTER("SendLastNewsletter"),
	SEND_MAILING("SendMailing"),
	SERVICE_MAIL("ServiceMail"),
	SUBSCRIBE_CUSTOMER("SubscribeCustomer"),
	UNSUBSCRIBE_CUSTOMER("UnsubscribeCustomer"),
	UPDATE_CUSTOMER("UpdateCustomer");
	
	private String name;
	
	ActionOperationType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static final ActionOperationType fromTypeName(final String name) {
		for(final ActionOperationType type : values()) {
			if(type.getName().equals(name)) {
				return type;
			}
		}
		
		return null;
	}
}
