/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.common;

import java.util.NoSuchElementException;
import java.util.Objects;

public enum MailingStatus {

	ACTIVE("mailing.status.active"),
	ADMIN("mailing.status.admin"),
	CANCELED("mailing.status.canceled"),
	INSUFFICIENT_VOUCHERS("mailing.status.insufficient-vouchers"),

	/** Mailing has been canceled (during sending) and copied. Sending of original mailing cannot be resumed. */
	CANCELED_AND_COPIED("mailing.status.canceledAndCopied"),
	
	DISABLE("mailing.status.disable"),
	EDIT("mailing.status.edit"),
	GENERATION_FINISHED("mailing.status.generation-finished"),
	IN_GENERATION("mailing.status.in-generation"),
	NEW("mailing.status.new"),
	NORECIPIENTS("mailing.status.norecipients"),
	READY("mailing.status.ready"),
	SCHEDULED("mailing.status.scheduled"),
	SENDING("mailing.status.sending"),
	SENT("mailing.status.sent"),
	TEST("mailing.status.test");
	
	private final String dbKey;
	private final String messageKey;
	
	MailingStatus(String dbKey) {
		this.dbKey = Objects.requireNonNull(dbKey);
		this.messageKey = this.dbKey;
	}
	
	public String getDbKey() {
		return dbKey;
	}
	
	public String getMessageKey() {
		return messageKey;
	}
	
	public static MailingStatus fromDbKey(final String key) {
		for(final MailingStatus status : values()) {
			if(status.dbKey.equals(key)) {
				return status;
			}
		}
		
		throw new NoSuchElementException();
	}
}
