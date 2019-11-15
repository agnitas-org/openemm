/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;

import com.agnitas.dao.ComServerStatusDao;

/**
 * DAO handler for status infos
 * This class is compatible with oracle and mysql datasources and databases
 */
public class ComServerStatusDaoImpl extends BaseDaoImpl implements ComServerStatusDao {
	private static final transient Logger logger = Logger.getLogger(ComServerStatusDaoImpl.class);
	
	private static final String DB_VERSION_TABLE = "agn_dbversioninfo_tbl";
	
	private static final String DB_VERSION_FIELD_VERSION = "version_number";
	private static final String DB_VERSION_FIELD_USER = "updating_user";
	private static final String DB_VERSION_FIELD_TIMESTAMP = "update_timestamp";
	
	private static final String[] DB_VERSION_FIELD_NAMES = new String[]{DB_VERSION_FIELD_VERSION, DB_VERSION_FIELD_USER, DB_VERSION_FIELD_TIMESTAMP};
	
	private static final String CHECK_DB_CONNECTION = "SELECT 1 FROM DUAL";
	private static final String SELECT_DB_VERSION_ORACLE = "SELECT " + StringUtils.join(DB_VERSION_FIELD_NAMES, ", ") + " FROM " + DB_VERSION_TABLE + " WHERE REGEXP_LIKE(" + DB_VERSION_FIELD_VERSION + ", '0*' || ? || '.0*' || ? || '.0*' || ?)";
	private static final String SELECT_DB_VERSION_ORACLE_HOTFIX = "SELECT " + StringUtils.join(DB_VERSION_FIELD_NAMES, ", ") + " FROM " + DB_VERSION_TABLE + " WHERE REGEXP_LIKE(" + DB_VERSION_FIELD_VERSION + ", '0*' || ? || '.0*' || ? || '.0*' || ? || '-hf' || ?)";
	private static final String SELECT_DB_VERSION_MYSQL = "SELECT " + StringUtils.join(DB_VERSION_FIELD_NAMES, ", ") + " FROM " + DB_VERSION_TABLE + " WHERE " + DB_VERSION_FIELD_VERSION + " REGEXP CONCAT('0*', ?, '.0*', ?, '.0*', ?)";
	private static final String SELECT_DB_VERSION_MYSQL_HOTFIX = "SELECT " + StringUtils.join(DB_VERSION_FIELD_NAMES, ", ") + " FROM " + DB_VERSION_TABLE + " WHERE " + DB_VERSION_FIELD_VERSION + " REGEXP CONCAT('0*', ?, '.0*', ?, '.0*', ?, '-hf', ?)";
	
	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection
	
	protected ConfigService configService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic

	@Override
	public boolean checkDatabaseConnection() {
		try {
			int result = selectInt(logger, CHECK_DB_CONNECTION);
			return result == 1;
		} catch (DataAccessException e) {
			return false;
		}
	}
	
	@Override
	public boolean checkDatabaseVersion(int majorVersion, int minorVersion, int microVersion, int hotfixVersion) {
		if (majorVersion < 0 || minorVersion < 0 || microVersion < 0) {
			return false;
		} else {
			try {
				List<Map<String, Object>> results;
				if (hotfixVersion > 0) {
					results = select(logger, (isOracleDB() ? SELECT_DB_VERSION_ORACLE_HOTFIX : SELECT_DB_VERSION_MYSQL_HOTFIX), majorVersion, minorVersion, microVersion, hotfixVersion);
				} else {
					results = select(logger, (isOracleDB() ? SELECT_DB_VERSION_ORACLE : SELECT_DB_VERSION_MYSQL), majorVersion, minorVersion, microVersion);
				}
				return results.size() > 0;
			} catch (DataAccessException e) {
				return false;
			}
		}
	}

	@Override
	public String getJobWorkerStatus(String jobWorkerName) {
		List<Map<String, Object>> result = select(logger, "SELECT * FROM job_queue_tbl WHERE description = ?", jobWorkerName);
		if (result.size() == 0) {
			return "Not available";
		} else if (result.size() > 1) {
			return "Invalid";
		} else {
			Map<String, Object> row = result.get(0);
			if (((Number) row.get("deleted")).intValue() > 0) {
				return "Deactivated";
			} else if (row.get("laststart") == null) {
				return "Never ran";
			} else if (((Date) row.get("laststart")).before(DateUtilities.getDateOfHoursAgo(1))) {
				return "Did not run within last hour";
			} else if (!"OK".equals(row.get("lastresult"))) {
				return "Error";
			} else {
				return "OK";
			}
		}
	}

	@Override
	public int executeUpdate(String updateStatement, Object... parameter) {
		return update(logger, updateStatement, parameter);
	}

	@Override
	public List<Map<String, Object>> select(String selectStatement, Object... parameter) {
		return select(logger, selectStatement, parameter);
	}

	@Override
	public int selectInt(String selectStatement, Object... parameter) {
		return selectInt(logger, selectStatement, parameter);
	}
	
	@Override
	public String getDbUrl() throws Exception {
		return DbUtilities.getDbUrl(getDataSource());
	}
    
    @Override
    public String getDbVendor() {
		DataSource dataSource = getDataSource();
		if (DbUtilities.checkDbVendorIsOracle(dataSource)) {
			return "Oracle";
		} else if (DbUtilities.checkDbVendorIsMariaDB(dataSource)) {
			return "MariaDB";
		}
		return "N/A";
    }
    
    @Override
    public Map<String, String> geDbInformation() {
		DataSource dataSource = getDataSource();
		Map<String, String> status = new HashMap<>();
		if (DbUtilities.checkDbVendorIsOracle(dataSource)) {
			String version = select(logger, "SELECT * FROM v$version WHERE rownum = 1", String.class);
			status.put("db.version", "Oracle " + version);
			return status;
		} else {
			String version = select(logger, "SELECT VERSION()", String.class);
			Map<String, Object> runningThreadsMap = selectSingleRow(logger, "SHOW GLOBAL STATUS like 'Threads_running'");
			Map<String, Object> createdThreadsMap = selectSingleRow(logger, "SHOW GLOBAL STATUS like 'Threads_created'");
			
			if (DbUtilities.checkDbVendorIsMariaDB(dataSource)) {
				status.put("db.version", "MariaDB " + version);
				try {
					status.put("mariadb.createdthreads", createdThreadsMap.get("Value").toString());
				} catch (Exception e) {
					status.put("mariadb.createdthreads", "Unknown");
				}
				try {
					status.put("mariadb.runningthreads", runningThreadsMap.get("Value").toString());
				} catch (Exception e) {
					status.put("mariadb.runningthreads", "Unknown");
				}
			} else {
				status.put("db.version", "MySQL " + version);
				try {
					status.put("mariadb.createdthreads", createdThreadsMap.get("VARIABLE_VALUE").toString());
				} catch (Exception e) {
					try {
						status.put("mariadb.createdthreads", createdThreadsMap.get("Value").toString());
					} catch (Exception e2) {
						status.put("mariadb.createdthreads", "Unknown");
					}
				}
				try {
					status.put("mariadb.runningthreads", runningThreadsMap.get("VARIABLE_VALUE").toString());
				} catch (Exception e) {
					try {
						status.put("mariadb.runningthreads", runningThreadsMap.get("Value").toString());
					} catch (Exception e2) {
						status.put("mariadb.runningthreads", "Unknown");
					}
				}
			}
			return status;
		}
	}
	
	@Override
	public int getLogEntryCount() {
		return select(logger, "SELECT COUNT(*) FROM emm_db_errorlog_tbl", Integer.class);
	}
	
	@Override
	public List<String> getErrorJobsStatuses() {
		String sql = "SELECT description FROM job_queue_tbl WHERE deleted = 0 AND ((lastresult != 'OK' AND lastresult IS NOT NULL) OR nextstart < ? OR (running = 1 AND laststart < ?))";
		return select(logger, sql, new StringRowMapper(), DateUtilities.getDateOfMinutesAgo(15), DateUtilities.getDateOfHoursAgo(5));
	}
	
	@Override
	public List<String> getDKIMKeys() {
		if (DbUtilities.checkIfTableExists(getDataSource(), "dkim_key_tbl")) {
			return select(logger, "SELECT DISTINCT domain FROM dkim_key_tbl WHERE valid_end IS NULL OR valid_end > CURRENT_TIMESTAMP ORDER BY domain", new StringRowMapper());
		} else {
			return new ArrayList<>();
		}
	}
	
	@Override
	public List<String> killRunningImports() {
		List<String> killedImportTables = new ArrayList<>();
		for (String tableName : select(logger, "SELECT temporary_table_name FROM import_temporary_tables", new StringRowMapper())) {
			try {
				update(logger, "DROP TABLE " + tableName);
				update(logger, "DELETE FROM import_temporary_tables WHERE temporary_table_name = ?", tableName);
			} catch(Exception e) {
				logger.error("Cannot clean up import table: " + tableName, e);
			}
			killedImportTables.add(tableName);
		}
		return killedImportTables;
	}
}
