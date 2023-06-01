/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.NoSuchElementException;
import java.util.Objects;

public enum MailingStatus {
	ACTIVE("mailing.status.active", "mailing.status.active"),
	ADMIN("mailing.status.admin", "mailing.status.admin"),
	CANCELED("mailing.status.canceled", "mailing.status.canceled"),
	
	/** Mailing has been canceled (during sending) and copied. Sending of original mailing cannot be resumed. */
	CANCELED_AND_COPIED("mailing.status.canceledAndCopied", "mailing.status.canceledAndCopied"),
	
	DISABLE("mailing.status.disable", "mailing.status.disable"),
	EDIT("mailing.status.edit", "mailing.status.edit"),
	GENERATION_FINISHED("mailing.status.generation-finished", "mailing.status.generation-finished"),
	IN_GENERATION("mailing.status.in-generation", "mailing.status.in-generation"),
	NEW("mailing.status.new", "mailing.status.new"),
	NORECIPIENTS("mailing.status.norecipients", "mailing.status.norecipients"),
	READY("mailing.status.ready", "mailing.status.ready"),
	SCHEDULED("mailing.status.scheduled", "mailing.status.scheduled"),
	SENDING("mailing.status.sending", "mailing.status.sending"),
	SENT("mailing.status.sent", "mailing.status.sent"),
	TEST("mailing.status.test", "mailing.status.test");
	
	private String dbKey;
	private String messageKey;
	
	MailingStatus(String dbKey, String messageKey) {
		this.dbKey = Objects.requireNonNull(dbKey);
		this.messageKey = Objects.requireNonNull(messageKey);
	}
	
	public String getDbKey() {
		return dbKey;
	}
	
	public String getMessageKey() {
		return messageKey;
	}
	
	public static final MailingStatus fromDbKey(final String key) {
		for(final MailingStatus status : values()) {
			if(status.dbKey.equals(key)) {
				return status;
			}
		}
		
		throw new NoSuchElementException();
	}
}
