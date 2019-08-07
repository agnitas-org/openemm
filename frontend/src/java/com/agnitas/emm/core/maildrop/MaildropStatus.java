/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop;

public enum MaildropStatus {
	WORLD('W'),
	TEST('T'),
	ADMIN('A'),
	DATE_BASED('R'), // formerly rule-based
	ACTION_BASED('E'), // formerly event-based
	ON_DEMAND('D'),
	PREDELIVERY('V'), // Verification
	
	UNKNOWN_C('C'), // Relict of old value for eventbased mailings. Will never be used again, New value is 'E'
	UNKNOWN_X('X'); // TODO: What is 'X'?
	
	private final char code;
	
	MaildropStatus(final char code) {
		this.code = code;
	}
	
	public char getCode() {
		return this.code;
	}
	
	public String getCodeString() {
		return Character.toString(this.code);
	}
	
	public static MaildropStatus fromCode(final char code) throws Exception {
		for(final MaildropStatus item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		throw new Exception("Invalid user MaildropStatus: " + code);
	}
	
	public static MaildropStatus fromName(final String name) throws Exception {
		for(final MaildropStatus item : values()) {
			if (item.name().equalsIgnoreCase(name)) {
				return item;
			}
		}
		throw new Exception("Invalid user MaildropStatus: " + name);
	}
}
