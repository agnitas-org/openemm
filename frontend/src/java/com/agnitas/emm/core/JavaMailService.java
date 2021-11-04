/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

public interface JavaMailService {
	/**
	 * Sends an email via Java (not via EMM Backend)
	 * 
	 * For default senderaddress (from address) leave fromAddress empty or null
	 * 
	 * For pure textmails leave bodyHtml empty or null
	 */
	boolean sendVelocityExceptionMail(int dkimCompanyID, String formUrl, Exception e);

	/**
	 * Sends an email via Java (not via EMM Backend)
	 * 
	 * For default senderaddress (from address) leave fromAddress empty or null
	 * 
	 * For pure textmails leave bodyHtml empty or null
	 */
	boolean sendExceptionMail(int dkimCompanyID, String errorText, Throwable e);

	/**
	 * Sends an email via Java (not via EMM Backend)
	 * 
	 * For default senderaddress (from address) leave fromAddress empty or null
	 * 
	 * For pure textmails leave bodyHtml empty or null
	 */
	boolean sendEmail(int dkimCompanyID, String toAddressList, String subject, String bodyText, String bodyHtml, JavaMailAttachment... attachments);

	/**
	 * Sends an email via Java (not via EMM Backend)
	 * 
	 * For default senderaddress (from address) leave fromAddress empty or null
	 * 
	 * For pure textmails leave bodyHtml empty or null
	 */
	boolean sendEmail(int dkimCompanyID, String toAddressList, String fromAddress, String replyToAddress, String subject, String bodyText, String bodyHtml, JavaMailAttachment... attachments);

	/**
	 * Sends an email via Java (not via EMM Backend)
	 * 
	 * For default senderaddress (from address) leave fromAddress empty or null
	 * 
	 * For pure textmails leave bodyHtml empty or null
	 */
	boolean sendEmail(int dkimCompanyID, String fromAddress, String fromName, String replyToAddress, String replyToName, String bounceAddress, String toAddressList, String ccAddressList, String subject, String bodyText, String bodyHtml, String charset, JavaMailAttachment... attachments);
}
