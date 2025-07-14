/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.util.Version;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.ServerCommand.Server;
import com.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is intended to simplify access to the config_tbl.
 */
public class ConfigTableDaoImpl extends BaseDaoImpl implements ConfigTableDao {

	@Override
	public Map<String, Map<Integer, String>> getAllEntriesForThisHost() {
		String sql;
		if (isOracleDB()) {
			sql = "SELECT TRIM(LEADING '.' FROM class || '.' || name) AS key_for_value, hostname, value AS value FROM config_tbl WHERE hostname IS NULL OR TRIM(hostname) IS NULL OR hostname = ? ORDER BY key_for_value, hostname";
		} else {
			sql = "SELECT TRIM(LEADING '.' FROM CONCAT(class, '.', name)) AS key_for_value, hostname, value AS value FROM config_tbl WHERE hostname IS NULL OR TRIM(hostname) = '' OR hostname = ? ORDER BY key_for_value, hostname";
		}
		
		List<Map<String, Object>> results = select(sql, AgnUtils.getHostName());
		Map<String, Map<Integer, String>> returnMap = new HashMap<>();
		for (Map<String, Object> resultRow : results) {
			String configValueName = (String) resultRow.get("key_for_value");
			String hostname = (String) resultRow.get("hostname");
			if (StringUtils.isBlank(hostname)) {
				hostname = null;
			}
			String value = (String) resultRow.get("value");
			
			Map<Integer, String> configValueMap = returnMap.get(configValueName);
			if (configValueMap == null) {
				configValueMap = new HashMap<>();
				returnMap.put(configValueName, configValueMap);
			}
			if (!configValueMap.containsKey(0) || hostname != null) {
				configValueMap.put(0, value);
			}
		}
		
		Map<Integer, String> configValueMapForDbType = returnMap.get(ConfigValue.DB_Vendor.toString());
		if (configValueMapForDbType == null) {
			configValueMapForDbType = new HashMap<>();
			returnMap.put(ConfigValue.DB_Vendor.toString(), configValueMapForDbType);
		}
		if (isOracleDB()) {
			configValueMapForDbType.put(0, "Oracle");
		} else {
			configValueMapForDbType.put(0, "MySQL");
		}
		
		return returnMap;
	}

	@DaoUpdateReturnValueCheck
	@Override
	public void storeEntry(String classString, String name, String hostName, String value, String description)  {
		if (StringUtils.isNotBlank(value) && value.startsWith(AgnUtils.getUserHomeDir())) {
			value = value.replace(AgnUtils.getUserHomeDir(), "${home}");
		}
		
		if (StringUtils.isBlank(hostName)) {
			List<Map<String, Object>> results = select("SELECT value FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", classString, name);
			if (results != null && results.size() > 0) {
				update("UPDATE config_tbl SET value = ?, description = ?, change_date = CURRENT_TIMESTAMP WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", value, description, classString, name);
			} else {
				update("INSERT INTO config_tbl (class, name, hostname, value, creation_date, change_date, description) VALUES (?, ?, NULL, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)", classString, name, value, description);
			}
		} else {
			List<Map<String, Object>> results = select("SELECT value FROM config_tbl WHERE class = ? AND name = ? AND hostname = ?", classString, name, hostName);
			if (results != null && results.size() > 0) {
				update("UPDATE config_tbl SET value = ?, description = ?, change_date = CURRENT_TIMESTAMP WHERE class = ? AND name = ? AND hostname = ?", value, description, classString, name, hostName);
			} else {
				update("INSERT INTO config_tbl (class, name, hostname, value, creation_date, change_date, description) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)", classString, name, hostName, value, description);
			}
		}
	}

	@Override
	public void deleteEntry(String classString, String name) {
		update("DELETE FROM config_tbl WHERE class = ? AND name = ?", classString, name);
	}

	@Override
	public int getJobqueueHostStatus(String hostName) {
		// Do not read this value by configservice, so it is not cached
		String configValueName = ConfigValue.JobQueueExecute.toString();
		String classValue = configValueName.substring(0, configValueName.indexOf("."));
		String nameValue = configValueName.substring(configValueName.indexOf(".") + 1);
		int returnValueWithoutExactHostnameMatch = 0;
		for (Map<String, Object> row : select("SELECT hostname, value FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR TRIM(hostname) IS NULL OR hostname = ?) ORDER BY hostname", classValue, nameValue, AgnUtils.getHostName())) {
			String hostname = (String) row.get("hostname");
			int value = Integer.parseInt((String) row.get("value"));
			if (AgnUtils.getHostName().equals(hostname)) {
				return value;
			} else {
				returnValueWithoutExactHostnameMatch = value;
			}
		}
		return returnValueWithoutExactHostnameMatch;
	}

	@Override
	public void checkAndSetReleaseVersion() {
		Server applicationType = ConfigService.getInstance().getApplicationType();

		String buildTimeString = ConfigService.getInstance().getValue(ConfigValue.BuildTime);
		Date buildTime;
		if (StringUtils.isNotBlank(buildTimeString)) {
			try {
				buildTime = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS).parse(buildTimeString);
			} catch (ParseException e) {
				try {
					buildTime = DateUtilities.parseUnknownDateFormat(buildTimeString);
				} catch (Exception e1) {
					logger.error("Unparseable BuldTime: " + buildTimeString);
					buildTime = null;
				}
			}
		} else {
			buildTime = null;
		}
		String buildHost = ConfigService.getInstance().getValue(ConfigValue.BuildHost);
		String buildUser = ConfigService.getInstance().getValue(ConfigValue.BuildUser);
		
		if (applicationType != null) {
			// Only keep data for 1 year
			update("DELETE FROM release_log_tbl WHERE host_name = ? AND application_name = ? AND startup_timestamp < ?", AgnUtils.getHostName(), applicationType.name(), DateUtilities.getDateOfDaysAgo(365));
			
			String versionString = ConfigService.getInstance().getValue(ConfigValue.ApplicationVersion);
			String lastStartedVersion = null;
			
			// Time of installation of lastStartedVersion, because it is only inserted in DB on version change
			Date lastStartedVersionStartupTime = null;
			
			List<Map<String, Object>> result;
			if (ConfigService.isOracleDB()) {
				result = select("SELECT version_number, startup_timestamp FROM (SELECT version_number, startup_timestamp FROM release_log_tbl WHERE host_name = ? AND application_name = ? ORDER BY startup_timestamp DESC) WHERE rownum <= 1", AgnUtils.getHostName(), applicationType.name());
			} else {
				result = select("SELECT version_number, startup_timestamp FROM release_log_tbl WHERE host_name = ? AND application_name = ? ORDER BY startup_timestamp DESC LIMIT 1", AgnUtils.getHostName(), applicationType.name());
			}
			if (result != null && result.size() > 0) {
				lastStartedVersion = (String) result.get(0).get("version_number");
				lastStartedVersionStartupTime = (Date) result.get(0).get("startup_timestamp");
			}
			if (!versionString.equals(lastStartedVersion)) {
				lastStartedVersionStartupTime = new Date();
				update("INSERT INTO release_log_tbl (host_name, application_name, version_number, build_time, build_host, build_user, startup_timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)", AgnUtils.getHostName(), applicationType.name(), versionString, buildTime, buildHost, buildUser, lastStartedVersionStartupTime);
			}
			ConfigService.setApplicationVersionInstallationTime(lastStartedVersionStartupTime);
		}
		
		// Check and set Runtime version
		String runtimeVersionFilePath = AgnUtils.getUserHomeDir() + "/conf/version.txt";
		if (new File(runtimeVersionFilePath).exists()) {
			Version runtimeVersion = null;
			try {
				runtimeVersion = new Version(FileUtils.readFileToString(new File(runtimeVersionFilePath), "UTF-8").trim());
			} catch (Exception e) {
				logger.error("Cannot store relealog data for runtime");
			}
			if (runtimeVersion != null) {
				String lastStartedRuntimeVersion;
				if (ConfigService.isOracleDB()) {
					lastStartedRuntimeVersion = selectObjectDefaultNull("SELECT * FROM (SELECT version_number FROM release_log_tbl WHERE host_name = ? AND application_name = ? ORDER BY startup_timestamp DESC) WHERE rownum <= 1", StringRowMapper.INSTANCE, AgnUtils.getHostName(), "RUNTIME");
				} else {
					lastStartedRuntimeVersion = selectObjectDefaultNull("SELECT version_number FROM release_log_tbl WHERE host_name = ? AND application_name = ? ORDER BY startup_timestamp DESC LIMIT 1", StringRowMapper.INSTANCE, AgnUtils.getHostName(), "RUNTIME");
				}
				if (!runtimeVersion.toString().equals(lastStartedRuntimeVersion)) {
					update("INSERT INTO release_log_tbl (host_name, application_name, version_number, build_time, build_host, build_user, startup_timestamp) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)", AgnUtils.getHostName(), "RUNTIME", runtimeVersion.toString(), null, null, null);
				}
			}
		}
		
		// Check and set Manual version if available
		String manualPath = ConfigService.getInstance().getValue(ConfigValue.ManualInstallPath) + "/de";
		if (new File(manualPath).exists()) {
			Version manualVersion = null;
			try {
				String manualVersionString = Paths.get(manualPath).toRealPath().toString();
				Matcher manualVersionMatcher = Pattern.compile(".*?([0-9]+[.][0-9]+[.][0-9]+(?:[.][0-9]+)?).*").matcher(manualVersionString);
				if (manualVersionMatcher.find()) {
					manualVersion = new Version(manualVersionMatcher.group(1));
				}
			} catch (Exception e) {
				logger.error("Cannot store relealog data for manual");
			}
			if (manualVersion != null) {
				String lastStartedManualVersion;
				if (ConfigService.isOracleDB()) {
					lastStartedManualVersion = selectObjectDefaultNull("SELECT * FROM (SELECT version_number FROM release_log_tbl WHERE host_name = ? AND application_name = ? ORDER BY startup_timestamp DESC) WHERE rownum <= 1", StringRowMapper.INSTANCE, AgnUtils.getHostName(), "MANUAL");
				} else {
					lastStartedManualVersion = selectObjectDefaultNull("SELECT version_number FROM release_log_tbl WHERE host_name = ? AND application_name = ? ORDER BY startup_timestamp DESC LIMIT 1", StringRowMapper.INSTANCE, AgnUtils.getHostName(), "MANUAL");
				}
				if (!manualVersion.toString().equals(lastStartedManualVersion)) {
					update("INSERT INTO release_log_tbl (host_name, application_name, version_number, build_time, build_host, build_user, startup_timestamp) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)", AgnUtils.getHostName(), "MANUAL", manualVersion.toString(), null, null, null);
				}
			}
		}
	}

	@Override
	public List<Map<String, Object>> getReleaseData(String hostNamePattern, String applicationTypePattern) {
		SqlPreparedStatementManager sqlPreparedStatementManager = new SqlPreparedStatementManager("SELECT startup_timestamp, host_name, application_name, version_number, build_time, build_host, build_user FROM release_log_tbl");
		if (StringUtils.isNotBlank(hostNamePattern)) {
			sqlPreparedStatementManager.addWhereClause("host_name LIKE ?", hostNamePattern.replace("?", "_").replace("*", "%"));
		}
		if (StringUtils.isNotBlank(applicationTypePattern)) {
			sqlPreparedStatementManager.addWhereClause("application_name LIKE ?", applicationTypePattern.replace("?", "_").replace("*", "%"));
		}
		
		return select(sqlPreparedStatementManager.getPreparedSqlString() + " ORDER BY startup_timestamp DESC", sqlPreparedStatementManager.getPreparedSqlParameters());
	}

	@Override
	public Date getCurrentDbTime() {
		if (isOracleDB()) {
			return select("SELECT CURRENT_TIMESTAMP FROM dual", Date.class);
		} else {
			return select("SELECT CURRENT_TIMESTAMP", Date.class);
		}
	}

	@Override
	public int getStartupCountOfLtsVersion(Version versionToCheck) {
		if (isOracleDB()) {
			return selectInt("SELECT COUNT(*) FROM release_log_tbl WHERE application_name = ? AND REGEXP_LIKE(version_number, '^0*' || ? || '.0*' || ? || '.000(.[0-9]+)*$')", Server.EMM.name(), versionToCheck.getMajorVersion(), versionToCheck.getMinorVersion());
		} else {
			return selectInt("SELECT COUNT(*) FROM release_log_tbl WHERE application_name = ? AND version_number REGEXP CONCAT('^0*', ?, '.0*', ?, '.000(.[0-9]+)*$')", Server.EMM.name(), versionToCheck.getMajorVersion(), versionToCheck.getMinorVersion());
		}
	}

	@Override
	public int getStartupCount() {
		return selectInt("SELECT COUNT(*) FROM release_log_tbl WHERE application_name = ?", Server.EMM.name());
	}
}
