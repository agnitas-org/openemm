/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.exceptions;

import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;

// TODO: Auto-generated Javadoc
/**
 * Base class for any exception that can be thrown when
 * working with {@link ComTrackableLinkService}.
 */
public class TrackableLinkException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 2482377026786503998L;

	/**
	 * Instantiates a new trackable link exception.
	 */
	public TrackableLinkException() {
		// Nothing to do here.
	}

	/**
	 * Instantiates a new trackable link exception.
	 *
	 * @param message error message
	 */
	public TrackableLinkException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new trackable link exception.
	 *
	 * @param cause the cause
	 */
	public TrackableLinkException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new trackable link exception.
	 *
	 * @param message error message
	 * @param cause the cause
	 */
	public TrackableLinkException(String message, Throwable cause) {
		super(message, cause);
	}

}
