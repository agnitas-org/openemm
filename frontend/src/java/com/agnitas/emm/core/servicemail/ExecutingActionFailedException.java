/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.servicemail;

/**
 * Exception indicating an error during execution of action by some
 * operations on service mails.
 */
public class ExecutingActionFailedException extends ServiceMailException {

	/** Serial version UID. */
	private static final long serialVersionUID = -3562165584007598045L;
	
	/** Action ID. */
	private final int actionID;
	
	/**
	 * Creates a new exception with given action ID.
	 * 
	 * @param actionID action ID
	 */
	public ExecutingActionFailedException(final int actionID) {
		super("Execution of action failed: " + actionID);
		
		this.actionID = actionID;
	}

	/**
	 * Returns action ID.
	 * 
	 * @return action ID
	 */
	public int getActionID() {
		return this.actionID;
	}

}
