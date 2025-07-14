/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.agnitas.service.JobWorker;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.SqlScriptReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.agnitas.util.Version;

/**
 * This MigrationJobWorker executes DBUpdate scriptse like emm-[oracle|mariadb]-migration-until-*.*.*.sql
 * 
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    (SELECT MAX(id) + 1, 'MigrationJobWorker', CURRENT_TIMESTAMP, null, 0, 'OK', 0, 0, 'MoTuWeThFr:1500', CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.MigrationJobWorker', 0  FROM job_queue_tbl);
 */
public class MigrationJobWorker extends JobWorker {
	private static final transient Logger logger = LogManager.getLogger(MigrationJobWorker.class);

	/**
	 Migration scripts are executed at least once.

	 After that initial execution, each subsequent run checks whether the current minimum application version
	 (from the release_log_tbl) is **less than or equal to** the "until-..." version specified in the script filename.

	 If the current version is greater than the script's "until" version, the script is skipped.

	 The current minimum application version is stored in the job parameters as "latest_min_app_version"
	 for comparison in the next JobWorker run.
	 */
	@Override
	public String runJob() throws Exception {
		Version latestMinAppVersion = null;
		String latestMinAppVersionString = getJob().getParameters().get("latest_min_app_version");
		if (StringUtils.isNotBlank(latestMinAppVersionString)) {
			latestMinAppVersion = new Version(latestMinAppVersionString);
		}
		
		DataSource datasource = daoLookupFactory.getBeanDataSource();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
		
		List<File> migrationScriptFiles = null;
		if (DbUtilities.checkDbVendorIsOracle(datasource)) {
			File applicationBasePath = new File(System.getProperty("user.home") + "/webapps");
			for (File applicationPath : applicationBasePath.listFiles()) {
				File oracleSqlScriptPath = new File(applicationPath + "/WEB-INF/sql/oracle");
				if (oracleSqlScriptPath.exists()) {
					String[] migrationScriptFileNames = oracleSqlScriptPath.list(new FilenameFilter() {
						@Override
						public boolean accept(File parentFile, String fileName) {
							return Pattern.matches("emm-oracle-migration-until-\\d+\\.\\d+(\\.\\d+)*\\.sql", fileName);
						}
					});
					migrationScriptFiles = new ArrayList<>();
					for (String migrationScriptFileName : migrationScriptFileNames) {
						migrationScriptFiles.add(new File(oracleSqlScriptPath + "/" + migrationScriptFileName));
					}
				}
			}
		} else {
			File applicationBasePath = new File(System.getProperty("user.home") + "/webapps");
			for (File applicationPath : applicationBasePath.listFiles()) {
				File mariadbSqlScriptPath = new File(applicationPath + "/WEB-INF/sql/mariadb");
				if (mariadbSqlScriptPath.exists()) {
					String[] migrationScriptFileNames = mariadbSqlScriptPath.list(new FilenameFilter() {
						@Override
						public boolean accept(File parentFile, String fileName) {
							return Pattern.matches("emm-mariadb-migration-until-\\d+\\.\\d+(\\.\\d+)*\\.sql", fileName);
						}
					});
					migrationScriptFiles = new ArrayList<>();
					for (String migrationScriptFileName : migrationScriptFileNames) {
						migrationScriptFiles.add(new File(mariadbSqlScriptPath + "/" + migrationScriptFileName));
					}
				}
			}
		}
		
		List<String> executedMigrationScriptFiles = new ArrayList<>();
		if (migrationScriptFiles != null && migrationScriptFiles.size() > 0) {
			List<Map<String, Object>> appVersions = jdbcTemplate.queryForList("SELECT MAX(version_number) AS version, application_name, host_name FROM release_log_tbl GROUP BY application_name, host_name");
			try {
				Version minAppVersion = null;
				for (Map<String, Object> row : appVersions) {
					Version appVersion = new Version((String) row.get("version"));
					if (minAppVersion == null || minAppVersion.compareTo(appVersion) > 0) {
						minAppVersion = appVersion;
					}
				}
				if (minAppVersion != null) {
					jobQueueDao.storeDynamicJobParameter(job.getId(), "latest_min_app_version", minAppVersion.toString());
				}
			} catch (Exception e) {
				logger.error("Cannot parse application version strings", e);
			}
			
			for (File migrationScriptFile : migrationScriptFiles) {
				Matcher matcher = Pattern.compile("\\d+\\.\\d+(\\.\\d+)*").matcher(migrationScriptFile.getName());
				matcher.find();
				Version untilVersion = new Version(matcher.group(0));
				
				if (latestMinAppVersion == null || latestMinAppVersion.compareTo(untilVersion) <= 0) {
					try (SqlScriptReader sqlScriptReader = new SqlScriptReader(new ByteArrayInputStream(FileUtils.readFileToByteArray(migrationScriptFile)))) {
						String nextStatementToExecute;
						while ((nextStatementToExecute = sqlScriptReader.readNextStatement()) != null) {
							jdbcTemplate.execute(nextStatementToExecute);
						}
						executedMigrationScriptFiles.add(migrationScriptFile.getName());
					} catch (Exception e) {
						throw new Exception("Error while executing migration script '" + migrationScriptFile.getAbsolutePath() + "': " + e.getMessage(), e);
					}
				}
			}
		}
		
		if (executedMigrationScriptFiles.size() > 0) {
			return "Executed " + StringUtils.join(executedMigrationScriptFiles, ", ");
		} else {
			return "No Migration script executed"; 
		}
	}
}
