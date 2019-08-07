/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.target.service.impl;

import org.agnitas.emm.core.target.dao.ExportPredefTargetGroupLocatorDao;
import org.agnitas.emm.core.target.exception.TargetGroupException;
import org.agnitas.emm.core.target.service.TargetGroupLocator;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link TargetGroupLocator} checking export profiles.
 */
public class ExportPredefTargetGroupLocator implements TargetGroupLocator {

	/**
	 * DAO for checking export profiles.
	 */
	private ExportPredefTargetGroupLocatorDao locatorDao;

	@Override
	public TargetDeleteStatus isTargetGroupCanBeDeleted(int companyID, int targetGroupID) throws TargetGroupException {
		return this.locatorDao.hasExportProfilesForTargetGroup(targetGroupID, companyID) ? TargetDeleteStatus.CANT_BE_DELETED : TargetDeleteStatus.CAN_BE_FULLY_DELETED_FROM_DB;
	}

	// -------------------------------------------------------- Dependency Injection
	/**
	 * Set DAO for checking export profiles.
	 * 
	 * @param dao DAO for checking export profiles
	 */
	@Required
	public void setExportPredefTargetGroupLocatorDao(ExportPredefTargetGroupLocatorDao dao) {
		this.locatorDao = dao;
	}
}
