/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.exception.target;

public class TargetGroupTooLargeException extends TargetGroupPersistenceException {

	/** Serial version UID. */
	private static final long serialVersionUID = -8313056180446167009L;
	
	private final int targetId;
	
	public TargetGroupTooLargeException(int targetId) {
		super("Target group too large: " + targetId);
		
		this.targetId = targetId;
	}
	
	public int getTargetId() {
		return this.targetId;
	}
}
