/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.exception;

/**
 * Exception indicating that a target group is unknown.
 */
public class TargetNotExistException extends RuntimeException {

	/**
	 * Serial version UID. 
	 */
	private static final long serialVersionUID = -859778424456594357L;

	/**
	 * Unknown target ID.
	 */
	private final Integer targetID;

	/**
	 * Creates a new exception.
	 * 
	 * @param targetID unknown target ID
	 */
	public TargetNotExistException(Integer targetID) {
		super("Targt group does not exist: " + targetID);
		
		this.targetID = targetID;
	}

	/**
	 * Returns unknown target ID.
	 * 
	 * @return unknown target ID
	 */
	public Integer getTargetID() {
		return targetID;
	}

}
