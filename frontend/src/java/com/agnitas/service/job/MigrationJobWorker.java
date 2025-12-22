/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.service.JobWorkerBase;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.ServerCommand.Server;
import com.agnitas.util.SqlScriptReader;
import com.agnitas.util.Version;
import com.agnitas.util.quartz.JobWorker;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This MigrationJobWorker executes DBUpdate scripts like emm-[oracle|mariadb]-migration-until-*.*.*.sql
 *
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    (SELECT MAX(id) + 1, 'MigrationJobWorker', CURRENT_TIMESTAMP, null, 0, 'OK', 0, 0, 'MoTuWeThFr:1500', CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.MigrationJobWorker', 0  FROM job_queue_tbl);
 */
@JobWorker("MigrationJobWorker")
public class MigrationJobWorker extends JobWorkerBase {

    private static final Logger logger = LogManager.getLogger(MigrationJobWorker.class);

    private static final String VERSION_REGEXP = "^[0-9]+(\\.[0-9]+)*[0-9]$";

    private JdbcTemplate jdbcTemplate;

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
        List<File> migrationScriptFiles = findMigrationScriptFiles(isOracleDb() ? "oracle" : "mariadb");
        if (!migrationScriptFiles.isEmpty()) {
            Version latestMinAppVersion = readLatestMinAppVersion();

            readMinAppVersion()
                    .ifPresent(v -> jobQueueDao.storeDynamicJobParameter(job.getId(), "latest_min_app_version", v.toString()));

            List<String> executedFiles = executeMigrationScripts(migrationScriptFiles, latestMinAppVersion);
            if (!executedFiles.isEmpty()) {
                return "Executed " + StringUtils.join(executedFiles, ", ");
            }
        }

        return "No Migration script executed";
    }

    private List<File> findMigrationScriptFiles(String dbVendorName) {
        List<File> migrationScriptFiles = null;

        File applicationBasePath = new File(System.getProperty("user.home") + "/webapps");
        for (File applicationPath : applicationBasePath.listFiles()) {
            File sqlScriptPath = new File("%s/WEB-INF/sql/%s".formatted(applicationPath, dbVendorName));
            if (sqlScriptPath.exists()) {
                String[] migrationScriptFileNames = sqlScriptPath.list((parentFile, fileName) ->
                        Pattern.matches("emm-%s-migration-until-\\d+\\.\\d+(\\.\\d+)*\\.sql".formatted(dbVendorName), fileName));
                migrationScriptFiles = new ArrayList<>();
                for (String migrationScriptFileName : migrationScriptFileNames) {
                    migrationScriptFiles.add(new File(sqlScriptPath + File.separator + migrationScriptFileName));
                }
            }
        }

        return migrationScriptFiles == null ? Collections.emptyList() : migrationScriptFiles;
    }

    private Optional<Version> readMinAppVersion() {
        try {
            return getMinReleaseVersion()
                    .map(Version::of);
        } catch (Exception e) {
            logger.error("Cannot parse application version strings", e);
            return Optional.empty();
        }
    }

    private Optional<String> getMinReleaseVersion() {
        String query = """
                SELECT MIN(version)
                FROM (
                         SELECT MAX(version_number) AS version
                         FROM release_log_tbl
                         WHERE application_name IN (?, ?, ?, ?)
                           AND startup_timestamp >= %s
                           AND %s
                             GROUP BY application_name, host_name
                     ) %s
                """
                .formatted(
                        isOracleDb() ? "SYSDATE - ?" : "NOW() - INTERVAL ? DAY",
                        isOracleDb()
                                ? "REGEXP_LIKE(version_number, '" + VERSION_REGEXP + "')"
                                : "version_number REGEXP '" + VERSION_REGEXP + "'",
                        isOracleDb() ? "" : "AS sub"
                );

        return getJdbcTemplate().queryForList(
                        query,
                        String.class,
                        Server.EMM.name(),
                        Server.STATISTICS.name(),
                        Server.RDIR.name(),
                        Server.WS.name(),
                        90
                )
                .stream()
                .filter(StringUtils::isNotBlank)
                .findAny();
    }

    private boolean isSqlUpdateMissing(String versionNumber) {
        String query = "SELECT COUNT(*) FROM agn_dbversioninfo_tbl WHERE version_number = ?";
        return getJdbcTemplate().queryForObject(query, Integer.class, versionNumber) == 0;
    }

    private List<String> executeMigrationScripts(List<File> scriptFiles, Version latestMinAppVersion) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Starting migration job | Current version: {} | Migration threshold version: {}",
                    configService.getValue(ConfigValue.ApplicationVersion), latestMinAppVersion);
        }

        List<String> executedFiles = new ArrayList<>();

        for (File migrationScriptFile : scriptFiles) {
            Version untilVersion = getVersionFromFile(migrationScriptFile);

            if (latestMinAppVersion == null || latestMinAppVersion.compareTo(untilVersion) <= 0) {
                try {
                    executeMigrationScript(migrationScriptFile);
                    executedFiles.add(migrationScriptFile.getName());
                } catch (Exception e) {
                    if (isSqlUpdateMissing(untilVersion.toString())) {
                        throw e;
                    }
                }
            }
        }

        return executedFiles;
    }

    private void executeMigrationScript(File migrationScriptFile) throws Exception {
        try (SqlScriptReader sqlScriptReader = new SqlScriptReader(new ByteArrayInputStream(Files.readAllBytes(migrationScriptFile.toPath())))) {
            String nextStatementToExecute;
            while ((nextStatementToExecute = sqlScriptReader.readNextStatement()) != null) {
                getJdbcTemplate().execute(nextStatementToExecute);
            }
        } catch (Exception e) {
            throw new Exception("Error while executing migration script '" + migrationScriptFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
    }

    private Version readLatestMinAppVersion() throws Exception {
        String latestMinAppVersionString = getJob().getParameters().get("latest_min_app_version");
        if (StringUtils.isBlank(latestMinAppVersionString)) {
            return null;
        }

        return new Version(latestMinAppVersionString);
    }

    private Version getVersionFromFile(File file) throws Exception {
        Matcher matcher = Pattern.compile("\\d+\\.\\d+(\\.\\d+)*").matcher(file.getName());
        matcher.find();
        return new Version(matcher.group(0));
    }

    private boolean isOracleDb() {
        return DbUtilities.checkDbVendorIsOracle(daoLookupFactory.getBeanDataSource());
    }

    private JdbcTemplate getJdbcTemplate() {
        if (this.jdbcTemplate == null) {
            this.jdbcTemplate = new JdbcTemplate(daoLookupFactory.getBeanDataSource());
        }

        return this.jdbcTemplate;
    }

}
