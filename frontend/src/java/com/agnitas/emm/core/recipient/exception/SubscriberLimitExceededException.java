/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.exception;

/*
 * FIXME Make exception extending Exception instead of RuntimeException.
 * 
 * Due to some ugly architecture decisions, this exception must be
 * derived from RuntimeException at the moment. 
 */

/**
 * Exception indicating that the maximum number of subscribers 
 * would be exceeded.
 */
public class SubscriberLimitExceededException extends RuntimeException {

	/** Serial version UID. */
	private static final long serialVersionUID = -954332945332438895L;
	
	private final int maximum;
	private final int actual;

	/**
	 * Creates a new instance.
	 */
	public SubscriberLimitExceededException(final int maximum, final int actual) {
		super(String.format("Subscriber limit exceeded (max: %d, actual: %d)", maximum, actual));
		
		this.maximum = maximum;
		this.actual = actual;
	}

	public int getMaximum() {
		return maximum;
	}

	public int getActual() {
		return actual;
	}
	
}
