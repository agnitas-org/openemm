/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.util;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;

/**
 * This class is intended to simplify access to the company_info_tbl
 */
public class CompanyInfoDao extends BaseDaoImpl {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(CompanyInfoDao.class);
	
	public Map<String, String> getAllEntries() throws SQLException {
		List<Map<String, Object>> results = select(logger, "SELECT company_id, cname, cvalue FROM company_info_tbl");
		Map<String, String> returnMap = new HashMap<>();
		for (Map<String, Object> resultRow : results) {
			String configValueName = (String) resultRow.get("cname");
			Number companyID = (Number) resultRow.get("company_id");
			if (companyID != null && companyID.intValue() != 0) {
				configValueName = configValueName + "." + companyID;
			}
			String value = (String) resultRow.get("cvalue");
			
			returnMap.put(configValueName, value);
		}
		return returnMap;
	}
	
	public final void writeConfigValue(final int companyID, final String name, final String value, final String description) {
		final int updated = updateExistingValue(companyID, name, value, description);
		
		if (updated == 0) {
			insertNewValue(companyID, name, value, description);
		}
	}
	
	private final int updateExistingValue(final int companyID, final String name, final String value, final String description) {
		final String sql = "UPDATE company_info_tbl SET cvalue = ?, description = ?, timestamp = CURRENT_TIMESTAMP WHERE company_id = ? AND cname = ?";
		return update(logger, sql, value, description, companyID, name);
	}
	
	private final void insertNewValue(final int companyID, final String name, final String value, final String description) {
		final String sql = "INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
		update(logger, sql, companyID, name, value, description);
	}

	public void deleteValue(final int companyID, final String configValueName) {
		final String sql = "DELETE FROM company_info_tbl WHERE company_id = ? AND cname = ?";
		update(logger, sql, companyID, configValueName);
	}

	public String getDescription(String name, int companyID) {
		return selectWithDefaultValue(logger, "SELECT description FROM company_info_tbl WHERE company_id = ? AND cname = ?", String.class, null, companyID, name);
	}
}
