/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mediatypes.dao;

/**
 * Exception indicating error during accessing media types.
 */
public class MediatypesDaoException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = -2271896955498916758L;

	public MediatypesDaoException() {
		// Nothing to do here
	}

	public MediatypesDaoException(String message) {
		super(message);
	}

	public MediatypesDaoException(Throwable cause) {
		super(cause);
	}

	public MediatypesDaoException(String message, Throwable cause) {
		super(message, cause);
	}

}
