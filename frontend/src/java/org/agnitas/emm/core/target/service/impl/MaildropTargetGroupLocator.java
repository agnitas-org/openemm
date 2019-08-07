/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.target.service.impl;

import org.agnitas.emm.core.target.dao.MaildropTargetGroupLocatorDao;
import org.agnitas.emm.core.target.exception.TargetGroupException;
import org.agnitas.emm.core.target.service.TargetGroupLocator;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link TargetGroupLocator} scanning maildrop entries.
 */
public class MaildropTargetGroupLocator implements TargetGroupLocator {

	/**
	 * DAO with specific methods for this locator.
	 */
	private MaildropTargetGroupLocatorDao locatorDao;

	@Override
	public TargetDeleteStatus isTargetGroupCanBeDeleted(int companyID, int targetGroupID) throws TargetGroupException {
		return this.locatorDao.hasMaildropEntriesForTargetGroup(companyID, targetGroupID) ? TargetDeleteStatus.CANT_BE_DELETED : TargetDeleteStatus.CAN_BE_FULLY_DELETED_FROM_DB;
	}
	
	// -------------------------------------------------------------------------- Dependency Injection
	/**
	 * Set DAO with specific methods for this locator.
	 * 
	 * @param dao DAO with specific methods
	 */
	@Required
	public void setMaildropTargetGroupLocatorDao(MaildropTargetGroupLocatorDao dao) {
		this.locatorDao = dao;
	}
}
