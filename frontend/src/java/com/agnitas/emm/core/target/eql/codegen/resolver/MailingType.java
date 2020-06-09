/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.resolver;

/**
 * Type of mailing.
 */
public enum MailingType {

	/** Regular mailing. */
	NORMAL(0, "mailing.Normal_Mailing"),
	
	/** Action-based mailing. */
	ACTION_BASED(1, "mailing.action.based.mailing"),
	
	/** Date-based mailing. */
	DATE_BASED(2, "mailing.Rulebased_Mailing"),
	
	/** Follow-Up mailing. */
	FOLLOW_UP(3, "mailing.Followup_Mailing"),
	
	/** Interval mailing. */
	INTERVAL(4, "mailing.Interval_Mailing");
	
	/** Magic number for mailing type. */
	private final int code;
	
	/** Messagekey for i18n. */
	private final String messagekey;
	
	/**
	 * Creates new enum element with given mailing type code.
	 * 
	 * @param code magic number of mailing type
	 */
	private MailingType(int code, String messagekey) {
		this.code = code;
		this.messagekey = messagekey;
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
	
	public static MailingType fromCode(final int code) {
		for (final MailingType mailingType : values()) {
			if (mailingType.code == code) {
				return mailingType;
			}
		}
		return null;
	}

	public static MailingType fromName(final String name) {
		for (final MailingType mailingType : values()) {
			if (mailingType.name().equalsIgnoreCase(name)) {
				return mailingType;
			}
		}
		return null;
	}
}
