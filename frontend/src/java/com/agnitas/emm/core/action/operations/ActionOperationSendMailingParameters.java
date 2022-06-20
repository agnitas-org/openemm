/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

public class ActionOperationSendMailingParameters extends AbstractActionOperationParameters {
	private int mailingID;
	private int delayMinutes;
	private String bcc;
	private boolean forActiveRecipients;

	public ActionOperationSendMailingParameters() {
		super(ActionOperationType.SEND_MAILING);
	}

	public int getMailingID() {
		return mailingID;
	}

	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	public int getDelayMinutes() {
		return delayMinutes;
	}

	public void setDelayMinutes(int delayMinutes) {
		this.delayMinutes = delayMinutes;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public boolean isForActiveRecipients() {
		return forActiveRecipients;
	}

	public void setForActiveRecipients(boolean forActiveRecipients) {
		this.forActiveRecipients = forActiveRecipients;
	}
}
