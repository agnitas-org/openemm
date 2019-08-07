/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.target.exception;

/**
 * Exception indicating that operation (like deleting) cannot be performed, because
 * target group is in use by some other item.
 */
public class TargetGroupIsInUseException extends TargetGroupException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = -6955856329115705793L;
	
	/**
	 * Affected target group ID.
	 */
	private final int targetGroupID;
	
	/**
	 * Creates a new expception.
	 * 
	 * @param targetGroupID affected target group ID
	 */
	public TargetGroupIsInUseException(int targetGroupID) {
		super("Target group is in use: " + targetGroupID);
		this.targetGroupID = targetGroupID;
	}

	/**
	 * Returns affected target group ID.
	 * 
	 * @return affected target group ID
	 */
	public int getTargetGroupID() {
		return this.targetGroupID;
	}

}
