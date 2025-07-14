/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.MailNotificationBufferDao;
import com.agnitas.emm.core.commons.encoder.HexEncoder;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;

import java.security.MessageDigest;
import java.util.Date;

/**
 * Data in mail_notification_buffer_tbl will be deleted after configured time by DB Cleaner (currently 14 days)
 */
public class MailNotificationBufferDaoImpl extends BaseDaoImpl implements MailNotificationBufferDao {

	@Override
	@DaoUpdateReturnValueCheck
	public boolean registerForSending(int retentionTimeMinutes, String toAddresses, String mailSubject, String textMailBody, String htmlMailBody) throws Exception {
		String textShortened;
		if (StringUtils.isNotBlank(textMailBody)) {
			textShortened = textMailBody;
		} else {
			if (htmlMailBody == null) {
				textShortened = "";
			} else {
				textShortened = htmlMailBody;
			}
		}
		// Cut the text if it is too long, but keep hash of full text in its end then, to make it unique overall
		if (textShortened.length() > 1000) {
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			md5Digest.update(textShortened.getBytes("UTF8"));
			String textShortenedMd5Hash = HexEncoder.toHexString(md5Digest.digest());
			textShortened = textShortened.substring(0, 900) + " ... Length: " + textShortened.length() + " Hash: " + textShortenedMd5Hash;
		}
		Date timeBeforeRetention = DateUtilities.getDateOfMinutesAgo(retentionTimeMinutes);
		int idOfExistingEntry = selectIntWithDefaultValue("SELECT MAX(id) FROM mail_notification_buffer_tbl WHERE recipients = ? AND subject = ? AND text = ? AND send_time >= ?", 0, toAddresses, mailSubject, textShortened, timeBeforeRetention);
		if (idOfExistingEntry > 0) {
			// rise the counter
			update("UPDATE mail_notification_buffer_tbl SET request_count = request_count + 1, last_request_time = ? WHERE id = ?", new Date(), idOfExistingEntry);
			return false;
		} else {
			// log new sending
			if (isOracleDB()) {
				int newId = selectInt("SELECT mail_noti_buffer_tbl_seq.NEXTVAL FROM DUAL");
				update("INSERT INTO mail_notification_buffer_tbl (id, recipients, subject, text, send_time, last_request_time, request_count) VALUES (" + AgnUtils.repeatString("?", 7, ", ") + ")", newId, toAddresses, mailSubject, textShortened, new Date(), new Date(), 1);
			} else {
				insertIntoAutoincrementMysqlTable("id", "INSERT INTO mail_notification_buffer_tbl (recipients, subject, text, send_time, last_request_time, request_count) VALUES (" + AgnUtils.repeatString("?", 6, ", ") + ")", toAddresses, mailSubject, textShortened, new Date(), new Date(), 1);
			}
			return true;
		}
	}
}
