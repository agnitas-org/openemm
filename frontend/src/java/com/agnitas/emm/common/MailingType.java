/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.common;

/**
 * Type of mailing.
 */
public enum MailingType {

	/** Regular mailing. */
	NORMAL(0, "mailing.Normal_Mailing", "regular"),
	
	/** Action-based mailing. */
	ACTION_BASED(1, "mailing.action.based.mailing", "action-based"),
	
	/** Date-based mailing. */
	DATE_BASED(2, "mailing.Rulebased_Mailing", "rule-based"),
	
	/** Follow-Up mailing. */
	FOLLOW_UP(3, "mailing.Followup_Mailing", "follow-up"),
	
	/** Interval mailing. */
	INTERVAL(4, "mailing.Interval_Mailing", "interval");
	
	/** Magic number for mailing type. */
	private final int code;
	
	/** Messagekey for i18n. */
	private final String messagekey;
	
	private final String webserviceCode;
	
	/**
	 * Creates new enum element with given mailing type code.
	 * 
	 * @param code magic number of mailing type
	 */
	MailingType(int code, String messagekey, final String webserviceCode) {
		this.code = code;
		this.messagekey = messagekey;
		this.webserviceCode = webserviceCode;
	}

	/**
	 * Returns the code for the mailing type.
	 * 
	 * @return code for mailing type
	 */
	public int getCode() {
		return code;
	}
	
	public String getMessagekey() {
		return messagekey;
	}
	
	public final String getWebserviceCode() {
		return this.webserviceCode;
	}
	
	public static MailingType fromCode(final int code) throws Exception {
		for (final MailingType mailingType : values()) {
			if (mailingType.code == code) {
				return mailingType;
			}
		}
		throw new Exception("Invalid MailingType code: " + code);
	}
	
	public static MailingType fromWebserviceCode(final String code) throws Exception {
		for (final MailingType mailingType : values()) {
			if (mailingType.webserviceCode.equals(code)) {
				return mailingType;
			}
		}
		throw new Exception("Invalid MailingType WS code: " + code);
	}

	public static MailingType fromName(final String name) throws Exception {
		if (name == null) {
			throw new Exception("Invalid empty MailingType name");
		}
		
		for (final MailingType mailingType : values()) {
			if (mailingType.name().replace("_", "").replace("-", "").equalsIgnoreCase(name.replace("_", "").replace("-", ""))) {
				return mailingType;
			}
		}
		
		if ("regular".equalsIgnoreCase(name)) {
			return MailingType.NORMAL;
		} else if ("rulebased".equalsIgnoreCase(name.replace("_", "").replace("-", ""))) {
			return MailingType.DATE_BASED;
		} else {
			throw new Exception("Invalid MailingType name: " + name);
		}
	}
}
