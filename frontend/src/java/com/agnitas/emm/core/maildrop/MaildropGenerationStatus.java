/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop;

import java.util.NoSuchElementException;

public enum MaildropGenerationStatus {
	SCHEDULED(0),
    NOW(1),
    WORKING(2),
    FINISHED(3),
    MANUALLY_SOLVED(4);

	private final int code;
	
	MaildropGenerationStatus(final int code) {
		this.code = code;
	}

	public static final MaildropGenerationStatus fromCodeOrNull(final int code) throws NoSuchElementException {
		for(final MaildropGenerationStatus value : values()) {
			if(value.code == code) {
				return value;
			}
		}
		
		return null;
	}
	
	public final int getCode() {
		return this.code;
	}

}
