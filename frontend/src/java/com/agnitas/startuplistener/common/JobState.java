/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.common;

import java.util.NoSuchElementException;

/**
 * State of startup job.
 */
public enum JobState {
	
	/** Job is ready for execution. */
	PENDING(0),
	
	/** Job is running. */
	IN_PROGRESS(1),
	
	/** Job has finished. */
	DONE(2),
	
	/** Job failed with exception. */
	FAILED(3);
	
	/** Numeric code. */
	private final int code;
	
	/** 
	 * Creates a new enum constant.
	 * 
	 * @param c numeric code
	 */
	private JobState(final int c) {
		this.code = c;
	}
	
	/**
	 * Returns the numeric code.
	 * 
	 * @return numeric code
	 */
	public final int getCode() {
		return this.code;
	}
	
	/**
	 * Returns the enum constant for given code.
	 * 
	 * @param code numeric code.
	 * 
	 * @return enum constant for given code
	 * 
	 * @throws NoSuchElementException if numeric code is unknown
	 */
	public static JobState fromCode(int code) {
		for(final JobState value : values()) {
			if(value.code == code) {
				return value;
			}
		}
		
		throw new NoSuchElementException();
	}
}
