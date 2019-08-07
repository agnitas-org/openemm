/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dao.impl;

import java.util.List;
import java.util.Map;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;

import com.agnitas.reporting.birt.external.dao.ComCompanyDao;

public class ComCompanyDaoImpl extends BaseDaoImpl implements ComCompanyDao {
	private static final transient Logger logger = Logger.getLogger(ComCompanyDaoImpl.class);

	@Override
	public boolean hasDeepTrackingTables(@VelocityCheck int companyID) {
		if (isOracleDB()) {
			try {
				int valNumTbl = selectInt(logger, "SELECT COUNT(table_name) FROM user_tables WHERE LOWER(table_name) = 'rdirlog_" + companyID + "_val_num_tbl'");
				if (valNumTbl == 0) {
					return false;
				}
			} catch (Exception e) {
				logger.error("Error:" + e, e);
			}

			try {
				int valAlphaTbl = selectInt(logger, "SELECT COUNT(table_name) FROM user_tables WHERE LOWER(table_name) = 'rdirlog_" + companyID + "_val_alpha_tbl'");
				if (valAlphaTbl == 0) {
					return false;
				}
			} catch (Exception e) {
				logger.error("Error:" + e, e);
			}

			try {
				int extLinkTbl = selectInt(logger, "SELECT COUNT(table_name) FROM user_tables WHERE LOWER(table_name) = 'rdirlog_" + companyID + "_ext_link_tbl'");
				if (extLinkTbl == 0) {
					return false;
				}
			} catch (Exception e) {
				logger.error("Error:" + e, e);
			}
		} else {
			try {
				List<Map<String, Object>> existList = select(logger, "SHOW TABLES LIKE 'rdirlog_" + companyID + "_val_num_tbl'");
				if (existList.size() == 0) {
					return false;
				}
			} catch (Exception e) {
				logger.error("Error:" + e, e);
			}

			try {
				List<Map<String, Object>> existList = select(logger, "SHOW TABLES LIKE 'rdirlog_" + companyID + "_val_alpha_tbl'");
				if (existList.size() == 0) {
					return false;
				}
			} catch (Exception e) {
				logger.error("Error:" + e, e);
			}

			try {
				List<Map<String, Object>> existList = select(logger, "show tables like 'rdirlog_" + companyID + "_ext_link_tbl'");
				if (existList.size() == 0) {
					return false;
				}
			} catch (Exception e) {
				logger.error("Error:" + e, e);
			}
		}

		return true;
	}
}
