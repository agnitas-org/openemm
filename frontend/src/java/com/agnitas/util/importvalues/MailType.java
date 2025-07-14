/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.importvalues;

public enum MailType {

	TEXT("MailType.0", 0),
	HTML("MailType.1", 1),
	HTML_OFFLINE("MailType.2", 2),
	
	/**
	 * Historical Mailtype, used in old mailingaccount data. Use HTML_OFFLINE for new data instead
	 */
	@Deprecated
	HTML_OFFLINE_HST("recipient.mailingtype.htmloffline", 3),
	
	/**
	 * Historical Mailtype
	 * Text-, HTML-, MobileHTML- and Offline-HTML-Mail
	 */
	@Deprecated
	MHTML_OFFLINE_HST("recipient.mailingtype.mhtml_and_offline", 4);

	private final String messageKey;
	private final int storageInt;

	MailType(String messageKey, int storageInt) {
		this.messageKey = messageKey;
		this.storageInt = storageInt;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public int getIntValue() {
		return storageInt;
	}

	public static MailType getFromString(String valueString) {
		for (MailType item : MailType.values()) {
			if (item.name().equalsIgnoreCase(valueString)) {
				return item;
			}
		}
		
		throw new IllegalArgumentException(String.format("Invalid string value '%s' for %s", valueString, MailType.class.getSimpleName()));
	}

	public static MailType getFromInt(int intValue) {
		for (MailType item : MailType.values()) {
			if (item.getIntValue() == intValue) {
				return item;
			}
		}
		
		throw new IllegalArgumentException(String.format("Invalid int value %d for %s", intValue, MailType.class.getSimpleName()));
	}
}
