/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.recipient.service;

import org.apache.commons.lang3.StringUtils;

public enum RecipientType {
		ALL_RECIPIENTS("E"),
		ADMIN_RECIPIENT("A"),
		TEST_RECIPIENT("T"),
		TEST_VIP_RECIPIENT("t"),
		NORMAL_RECIPIENT("W"),
		NORMAL_VIP_RECIPIENT("w");
		
		private final String letter;
		
		RecipientType(String letter) {
			this.letter = letter;
		}
		
		public static RecipientType getRecipientTypeByLetter(String letter) {
			letter = StringUtils.defaultIfEmpty(letter, "E");
			for (RecipientType type: RecipientType.values()) {
				if (type.letter.equals(letter)) {
					return type;
				}
			}
			
			return ALL_RECIPIENTS;
		}
	
	public String getLetter() {
		return letter;
	}
}
