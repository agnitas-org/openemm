/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.util.DbUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.dao.ComDkimDao;

/**
 * DAO handler for DKIM-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class ComDkimDaoImpl extends BaseDaoImpl implements ComDkimDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComDkimDaoImpl.class);

	@Override
	public boolean existsDkimKeyForDomain(int companyID, String domainname) {
		if (companyID <= 0 || StringUtils.isEmpty(domainname)) {
			return false;
		} else if (!DbUtilities.checkIfTableExists(getDataSource(), "dkim_key_tbl")) {
			return false;
		} else {
			return selectInt(logger, "SELECT COUNT(*) FROM dkim_key_tbl WHERE company_id = ? AND domain = ? AND (valid_start IS NULL OR valid_start <= CURRENT_TIMESTAMP) AND (valid_start IS NULL OR valid_end >= CURRENT_TIMESTAMP)", companyID, domainname) > 0;
		}
	}
	
	@Override
	public boolean deleteDkimKeyByCompany(int companyID) {
		if (companyID <= 0) {
			return false;
		} else if (!DbUtilities.checkIfTableExists(getDataSource(), "dkim_key_tbl")) {
			return false;
		} else {
			try {
				update(logger, "DELETE from dkim_key_tbl WHERE company_id = ?", companyID);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}
}
