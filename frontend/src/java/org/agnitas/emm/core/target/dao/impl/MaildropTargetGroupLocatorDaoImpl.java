/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.target.dao.impl;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.target.dao.MaildropTargetGroupLocatorDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;

/**
 * Implementation of {@link MaildropTargetGroupLocatorDao}.
 */
public class MaildropTargetGroupLocatorDaoImpl extends BaseDaoImpl implements MaildropTargetGroupLocatorDao {

	private static final transient Logger logger = LogManager.getLogger(MaildropTargetGroupLocatorDaoImpl.class);
	
	@Override
	public boolean hasMaildropEntriesForTargetGroup(@VelocityCheck int companyID, int targetGroupID) {
		int count = selectInt(logger, "SELECT count(*) FROM maildrop_status_tbl WHERE company_id=? AND genstatus<>? AND admin_test_target_id=?", companyID, MaildropGenerationStatus.FINISHED.getCode(), targetGroupID);

		return count > 0;
	}
}
