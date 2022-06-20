/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop;

public enum MaildropGenerationStatus {
	SCHEDULED(0, false),
    NOW(1, false),
    WORKING(2, false),
    FINISHED(3, true),
    MANUALLY_SOLVED(4, true);

	/** Numeric code as used in database. */
	private final int code;
	
	/** Flag to mark if state is final. Final state do not have any successor state. */
	private final boolean finalState;
	
	MaildropGenerationStatus(final int code, final boolean finalState) {
		this.code = code;
		this.finalState = finalState;
	}

	public static final MaildropGenerationStatus fromCodeOrNull(final int code) {
		for(final MaildropGenerationStatus value : values()) {
			if(value.code == code) {
				return value;
			}
		}
		
		return null;
	}
	
	public static final boolean isFinalState(final int code) {
		final MaildropGenerationStatus status = fromCodeOrNull(code);
		
		if(status == null) {
			return false;
		}
		
		return status.isFinalState();
	}
	
	public final int getCode() {
		return this.code;
	}

	public final boolean isFinalState() {
		return this.finalState;
	}
}
