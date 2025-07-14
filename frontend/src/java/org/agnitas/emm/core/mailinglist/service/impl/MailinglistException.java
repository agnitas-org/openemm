/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailinglist.service.impl;

public class MailinglistException extends Exception {
	private static final long serialVersionUID = 2425424172301124279L;
	
	private final int mailinglistID;
	private final int companyID;
	
	public MailinglistException(final int mailinglistID, final int companyID) {
		super();
		
		this.mailinglistID = mailinglistID;
		this.companyID = companyID;
	}
	
	public MailinglistException(final int mailinglistID, final int companyID, final String message) {
		super(message);
		
		this.mailinglistID = mailinglistID;
		this.companyID = companyID;
	}
	
	
	public MailinglistException(final int mailinglistID, final int companyID, final String message, final Throwable cause) {
		super(message, cause);
		
		this.mailinglistID = mailinglistID;
		this.companyID = companyID;
	}
	
	public MailinglistException(final int mailinglistID, final int companyID, final Throwable cause) {
		super(cause);
		
		this.mailinglistID = mailinglistID;
		this.companyID = companyID;
	}

	public int getMailinglistID() {
		return mailinglistID;
	}
	
	public final int getCompanyID() {
		return this.companyID;
	}
}
