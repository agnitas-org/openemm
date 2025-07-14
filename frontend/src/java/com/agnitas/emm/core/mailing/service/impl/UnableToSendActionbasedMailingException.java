/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import com.agnitas.emm.core.mailing.service.SendActionbasedMailingException;

public class UnableToSendActionbasedMailingException extends SendActionbasedMailingException {
	private static final long serialVersionUID = 7826105130693434008L;
	
	private final int mailingID;
	private final int customerID;
	
	public UnableToSendActionbasedMailingException(final int mailingID, final int customerID) {
		super(String.format("Unable to send action-based mailing %d (customer %d)", mailingID, customerID));
		
		this.mailingID = mailingID;
		this.customerID = customerID;
	}
	
	public UnableToSendActionbasedMailingException(final int mailingID, final int customerID, final Throwable cause) {
		super(String.format("Unable to send action-based mailing %d (customer %d)", mailingID, customerID), cause);
		
		this.mailingID = mailingID;
		this.customerID = customerID;
	}
	
	public int getMailingID() {
		return this.mailingID;
	}
	
	public int getCustomerID() {
		return this.customerID;
	}
}
