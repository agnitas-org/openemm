/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.target.dao;

import org.agnitas.emm.core.target.service.impl.ExportPredefTargetGroupLocator;
import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Interface for highly specific methods for {@link ExportPredefTargetGroupLocator}.
 */
public interface ExportPredefTargetGroupLocatorDao {

	/**
	 * Check, if there is an export profile, that uses the given target group.
	 * Deleted export profiles are ignored.
	 * 
	 * @param targetGroupID ID of target group
	 * @param companyID company ID of profiles to check
	 * 
	 * @return true, if there is an export profile, that uses the given target group.
	 */
	public boolean hasExportProfilesForTargetGroup(int targetGroupID, @VelocityCheck int companyID);
	
}
