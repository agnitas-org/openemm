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
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;

import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;

/**
 * This class is intended to simplify access to the config_tbl.
 */
public class ConfigTableDaoImpl extends BaseDaoImpl implements ConfigTableDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ConfigTableDaoImpl.class);
	
	private static final String SELECT_ALL_SIMPLIFIED_ORACLE = "SELECT TRIM(LEADING '.' FROM class || '.' || name) AS key_for_value, value AS value FROM config_tbl";
	private static final String SELECT_ALL_SIMPLIFIED_MYSQL = "SELECT TRIM(LEADING '.' FROM CONCAT(class, '.', name)) AS key_for_value, value AS value FROM config_tbl";
	
	private static final String SELECT_VALUE = "SELECT value FROM config_tbl WHERE class = ? AND name = ?";
	private static final String UPDATE_VALUE = "UPDATE config_tbl SET value = ? WHERE class = ? AND name = ?";
	private static final String INSERT_VALUE = "INSERT INTO config_tbl (class, name, value) VALUES (?, ?, ?)";
	private static final String DELETE_VALUE = "DELETE FROM config_tbl WHERE class = ? AND name = ?";

	@Override
	public Map<String, String> getAllEntries() throws SQLException {
		List<Map<String, Object>> results = select(logger, isOracleDB() ? SELECT_ALL_SIMPLIFIED_ORACLE : SELECT_ALL_SIMPLIFIED_MYSQL);
		Map<String, String> returnMap = new HashMap<>();
		for (Map<String, Object> resultRow : results) {
			returnMap.put((String) resultRow.get("key_for_value"), (String) resultRow.get("value"));
		}
		
		if (isOracleDB()) {
			returnMap.put(ConfigValue.DB_Vendor.toString(), "Oracle");
		} else {
			returnMap.put(ConfigValue.DB_Vendor.toString(), "MySQL");
		}
		
		return returnMap;
	}

	@DaoUpdateReturnValueCheck
	@Override
	public void storeEntry(String classString, String name, String value)  {
		List<Map<String, Object>> results = select(logger, SELECT_VALUE, classString, name);
		if (results != null && results.size() > 0) {
			update(logger, UPDATE_VALUE, value, classString, name);
		} else {
			update(logger, INSERT_VALUE, classString, name, value);
		}
	}

	@Override
	public void deleteEntry(String classString, String name) {
		update(logger, DELETE_VALUE, classString, name);
	}
}
