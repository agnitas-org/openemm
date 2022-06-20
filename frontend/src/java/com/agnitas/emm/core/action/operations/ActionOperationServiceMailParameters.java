/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

public class ActionOperationServiceMailParameters extends AbstractActionOperationParameters {
	private String textMail;
	private String subjectLine;
	private String toAddress;
	private String fromAddress;
	private String replyAddress;
	private int mailtype;
	private String htmlMail;

	public ActionOperationServiceMailParameters() {
		super(ActionOperationType.SERVICE_MAIL);
	}
	
	public String getTextMail() {
		return textMail;
	}

	public void setTextMail(String textMail) {
		this.textMail = textMail;
	}

	public String getSubjectLine() {
		return subjectLine;
	}

	public void setSubjectLine(String subjectLine) {
		this.subjectLine = subjectLine;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public int getMailtype() {
		return mailtype;
	}

	public void setMailtype(int mailtype) {
		this.mailtype = mailtype;
	}

	public String getHtmlMail() {
		return htmlMail;
	}

	public void setHtmlMail(String htmlMail) {
		this.htmlMail = htmlMail;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getReplyAddress() {
		return replyAddress;
	}

	public void setReplyAddress(String replyAddress) {
		this.replyAddress = replyAddress;
	}
}
