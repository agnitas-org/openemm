/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.servicemail;

/**
 * Exception indicating an unknown action ID used for some
 * operations on service mails.
 */
public class UnknownActionIdException extends ServiceMailException {

	/** Serial version UID. */
	private static final long serialVersionUID = -9203370714092981245L;
	
	/** Action ID. */
	private final int actionID;
	
	/**
	 * Creates a new exception with given unknown action ID.
	 * 
	 * @param actionID unknown action ID
	 */
	public UnknownActionIdException(final int actionID) {
		super("Unknown action ID: " + actionID);
		
		this.actionID = actionID;
	}

	/**
	 * Returns unknown action ID.
	 * 
	 * @return unknown action ID
	 */
	public int getActionID() {
		return this.actionID;
	}

}
