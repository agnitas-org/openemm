/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.util;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.DateRowMapper;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is intended to simplify access to the company_info_tbl
 */
public class CompanyInfoDao extends BaseDaoImpl {

	public Map<String, Map<Integer, String>> getAllEntriesForThisHost() {
		String sql;
		if (isOracleDB()) {
			sql = "SELECT cname, company_id, hostname, cvalue FROM company_info_tbl WHERE hostname IS NULL OR TRIM(hostname) IS NULL OR hostname = ? ORDER BY cname, company_id, hostname";
		} else {
			sql = "SELECT cname, company_id, hostname, cvalue FROM company_info_tbl WHERE hostname IS NULL OR TRIM(hostname) = '' OR hostname = ? ORDER BY cname, company_id, hostname";
		}
		
		List<Map<String, Object>> results = select(sql, AgnUtils.getHostName());
		Map<String, Map<Integer, String>> returnMap = new HashMap<>();
		for (Map<String, Object> resultRow : results) {
			String configValueName = (String) resultRow.get("cname");
			Number companyID = (Number) resultRow.get("company_id");
			int companyIDInt = 0;
			if (companyID != null && companyID.intValue() != 0) {
				companyIDInt = companyID.intValue();
			}
			String hostname = (String) resultRow.get("hostname");
			if (StringUtils.isBlank(hostname)) {
				hostname = null;
			}
			String value = (String) resultRow.get("cvalue");
			
			Map<Integer, String> configValueMap = returnMap.get(configValueName);
			if (configValueMap == null) {
				configValueMap = new HashMap<>();
				returnMap.put(configValueName, configValueMap);
			}
			if (!configValueMap.containsKey(companyIDInt) || hostname != null) {
				configValueMap.put(companyIDInt, value);
			}
		}
		return returnMap;
	}

	public Date getChangeDate(ConfigValue configValue, int companyID) {
		String query = "SELECT timestamp FROM company_info_tbl WHERE company_id = ? AND cname = ?";
		return selectObjectDefaultNull(query, DateRowMapper.INSTANCE, companyID, configValue.getName());
	}
	
	public final void writeConfigValue(final int companyID, final String name, final String value, final String description) {
		final int updated = updateExistingValue(companyID, name, value, description);
		
		if (updated == 0) {
			insertNewValue(companyID, name, value, description);
		}
	}
	
	private int updateExistingValue(final int companyID, final String name, final String value, final String description) {
		final String sql = "UPDATE company_info_tbl SET cvalue = ?, description = ?, timestamp = CURRENT_TIMESTAMP WHERE company_id = ? AND cname = ?";
		return update(sql, value, description, companyID, name);
	}
	
	private void insertNewValue(final int companyID, final String name, final String value, final String description) {
		final String sql = "INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
		update(sql, companyID, name, value, description);
	}

	public void deleteValue(final int companyID, final String configValueName) {
		final String sql = "DELETE FROM company_info_tbl WHERE company_id = ? AND cname = ?";
		update(sql, companyID, configValueName);
	}

	public String getDescription(String name, int companyID) {
		return selectWithDefaultValue("SELECT description FROM company_info_tbl WHERE company_id = ? AND cname = ?", String.class, null, companyID, name);
	}
	
	public String getValue(String name, int companyID) {
		return selectWithDefaultValue("SELECT cvalue FROM company_info_tbl WHERE company_id = ? AND cname = ?", String.class, null, companyID, name);
	}
}
