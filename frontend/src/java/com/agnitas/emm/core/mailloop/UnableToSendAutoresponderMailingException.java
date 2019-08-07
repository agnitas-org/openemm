/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailloop;

/**
 * Exception indicating an error sending an auto-responder mail.
 */
@Deprecated
public class UnableToSendAutoresponderMailingException extends MailloopException {

	/** Serial version UID. */
	private static final long serialVersionUID = -2264742346559234082L;
	
	/** ID of auto-responder mailing. */
	private final int mailingID;
	
	/** ID of recipient. */
	private final int customerID;
	
	/** ID of mailloop. */
	private final int mailloopID;
	
	/**
	 * Creates new exception indicating an error sending an auto-responder mailing.
	 * 
	 * @param mailingID ID of auto-responder mailing
	 * @param customerID ID of recipient
	 * @param mailloopID ID of mailloop
	 * @param cause cause
	 */
	public UnableToSendAutoresponderMailingException(final int mailingID, final int customerID, final int mailloopID, Throwable cause) {
		super(String.format("Unable to send auto-responder mailing %d to customer %d (mailloop %d)", mailingID, customerID, mailloopID), cause);
		
		this.mailingID = mailingID;
		this.customerID = customerID;
		this.mailloopID = mailloopID;
	}

	/**
	 * Creates new exception indicating an error sending an auto-responder mailing.
	 * 
	 * @param mailingID ID of auto-responder mailing
	 * @param customerID ID of recipient
	 * @param mailloopID ID of mailloop
	 */
	public UnableToSendAutoresponderMailingException(final int mailingID, final int customerID, final int mailloopID) {
		super(String.format("Unable to send auto-responder mailing %d to customer %d (mailloop %d)", mailingID, customerID, mailloopID));
		
		this.mailingID = mailingID;
		this.customerID = customerID;
		this.mailloopID = mailloopID;
	}

	/**
	 * Returns ID of auto-responder mailing.
	 * 
	 * @return ID of auto-responder mailing
	 */
	public int getMailingID() {
		return mailingID;
	}

	/**
	 * Returns ID of recipient.
	 * 
	 * @return ID of recipient
	 */
	public int getCustomerID() {
		return customerID;
	}

	/**
	 * Returns ID of mailloop.
	 * 
	 * @return ID of mailloop
	 */
	public int getMailloopID() {
		return mailloopID;
	}
	
}
