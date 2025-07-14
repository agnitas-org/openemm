/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.dao.MailNotificationBufferDao;
import com.agnitas.emm.core.JavaMailService;

public class MailNotificationService {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(MailNotificationService.class);
	
	private static final int RETENTION_TIME_MINUTES = 30;
	
	protected MailNotificationBufferDao mailNotificationBufferDao;
	
	protected JavaMailService javaMailService;

	public void setMailNotificationBufferDao(MailNotificationBufferDao mailNotificationBufferDao) {
		this.mailNotificationBufferDao = mailNotificationBufferDao;
	}

	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}
	
	public boolean sendNotificationMailWithDuplicateRetention(int companyID, String toAddresses, String mailSubject, String htmlMailBody) throws Exception {
		return sendNotificationMailWithDuplicateRetention(companyID, toAddresses, mailSubject, null, htmlMailBody);
	}

	public boolean sendNotificationMailWithDuplicateRetention(int companyID, String toAddresses, String mailSubject, String textMailBody, String htmlMailBody) throws Exception {
		if (mailNotificationBufferDao.registerForSending(RETENTION_TIME_MINUTES, toAddresses, mailSubject, textMailBody, htmlMailBody)) {
			if (javaMailService.sendEmail(companyID, toAddresses, mailSubject, textMailBody, htmlMailBody)) {
				return true;
			} else {
				logger.error("Cannot send notification mail: " + mailSubject);
				throw new Exception("Cannot send notification mail");
			}
		} else {
			return false;
		}
	}
}
