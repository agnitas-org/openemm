/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.target.service;

import org.agnitas.emm.core.target.exception.TargetGroupException;

/**
 * Interface for code to locate target groups.
 */
public interface TargetGroupLocator {

	/**
	 * Checks, if th given target group can be deleted
	 * @return {@link TargetDeleteStatus}
	 */
	public TargetDeleteStatus isTargetGroupCanBeDeleted(int companyID, int targetGroupID) throws TargetGroupException;

	/**
	 * Can target group be deleted or not
	 */
	enum TargetDeleteStatus {

		CAN_BE_FULLY_DELETED_FROM_DB,
		CAN_BE_MARKED_AS_DELETED,
		CANT_BE_DELETED;
	}
	
}
