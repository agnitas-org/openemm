/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.commons.util.ConfigKey;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;

/**
 * This class is intended to simplify access to the config_tbl.
 */
public class ConfigTableDaoImpl extends BaseDaoImpl implements ConfigTableDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ConfigTableDaoImpl.class);

	@Override
	public Map<ConfigKey, String> getAllEntries() throws SQLException {
		List<Map<String, Object>> results;
		if (isOracleDB()) {
			results = select(logger, "SELECT TRIM(LEADING '.' FROM class || '.' || name) AS key_for_value, hostname, value AS value FROM config_tbl");
		} else {
			results = select(logger, "SELECT TRIM(LEADING '.' FROM CONCAT(class, '.', name)) AS key_for_value, hostname, value AS value FROM config_tbl");
		}
		Map<ConfigKey, String> returnMap = new HashMap<>();
		for (Map<String, Object> resultRow : results) {
			returnMap.put(new ConfigKey((String) resultRow.get("key_for_value"), 0, (String) resultRow.get("hostname")), (String) resultRow.get("value"));
		}
		
		if (isOracleDB()) {
			returnMap.put(new ConfigKey(ConfigValue.DB_Vendor.toString(), 0, null), "Oracle");
		} else {
			returnMap.put(new ConfigKey(ConfigValue.DB_Vendor.toString(), 0, null), "MySQL");
		}
		
		return returnMap;
	}

	@DaoUpdateReturnValueCheck
	@Override
	public void storeEntry(String classString, String name, String value)  {
		storeEntry(classString, name, null, value);
	}

	@DaoUpdateReturnValueCheck
	@Override
	public void storeEntry(String classString, String name, String hostName, String value)  {
		if (StringUtils.isBlank(hostName)) {
			List<Map<String, Object>> results = select(logger, "SELECT value FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", classString, name);
			if (results != null && results.size() > 0) {
				update(logger, "UPDATE config_tbl SET value = ? WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", value, classString, name);
			} else {
				update(logger, "INSERT INTO config_tbl (class, name, hostname, value) VALUES (?, ?, NULL, ?)", classString, name, value);
			}
		} else {
			List<Map<String, Object>> results = select(logger, "SELECT value FROM config_tbl WHERE class = ? AND name = ? AND hostname = ?", classString, name, hostName);
			if (results != null && results.size() > 0) {
				update(logger, "UPDATE config_tbl SET value = ? WHERE class = ? AND name = ? AND hostname = ?", value, classString, name, hostName);
			} else {
				update(logger, "INSERT INTO config_tbl (class, name, hostname, value) VALUES (?, ?, ?, ?)", classString, name, hostName, value);
			}
		}
	}

	@Override
	public void deleteEntry(String classString, String name) {
		update(logger, "DELETE FROM config_tbl WHERE class = ? AND name = ?", classString, name);
	}

	@Override
	public int getJobqueueHostStatus(String hostName) {
		return selectIntWithDefaultValue(logger, "SELECT MIN(value) FROM config_tbl WHERE class = ? AND name = ?", 0, "system", hostName + ".IsActive");
	}
}
