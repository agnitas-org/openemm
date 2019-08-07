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
	NORMAL(0),
	
	/** Action-based mailing. */
	ACTION_BASED(1),
	
	/** Date-based mailing. */
	DATE_BASED(2),
	
	/** Follow-Up mailing. */
	FOLLOW_UP(3),
	
	/** Interval mailing. */
	INTERVAL(4);
	
	/** Magic number for mailing type. */
	private final int code;
	
	/**
	 * Creates new enum element with given mailing type code.
	 * 
	 * @param code magic number of mailing type
	 */
	MailingType(int code) {
		this.code = code;
	}

	/**
	 * Returns the code for the mailing type.
	 * 
	 * @return code for mailing type
	 */
	public int getCode() {
		return this.code;
	}
	
	public static MailingType fromCode(final int code) {
		for (final MailingType mt : values()) {
			if (mt.code == code) {
				return mt;
			}
		}
		return null;
	}

	public static MailingType fromName(final String name) {
		for (final MailingType mt : values()) {
			if (mt.name().equalsIgnoreCase(name)) {
				return mt;
			}
		}
		return null;
	}
}
