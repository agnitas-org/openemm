/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.JobQueueDao;
import com.agnitas.emm.core.serverstatus.forms.JobQueueOverviewFilter;
import com.agnitas.service.JobDto;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * DAO handler for JobDto-Objects
 */
public class JobQueueDaoImpl extends BaseDaoImpl implements JobQueueDao {

	@Override
	public List<JobDto> readUpcomingJobsForExecutionByRunClass() {
		return select("""
				SELECT *
				FROM job_queue_tbl
				WHERE running <= 0
				  AND nextStart IS NOT NULL
				  AND nextStart < CURRENT_TIMESTAMP
				  AND deleted <= 0
				  AND (lastResult IS NULL OR lastResult = 'OK' OR startAfterError > 0)
				  AND runClass IS NOT NULL
				""", new Job_RowMapper());
	}

	@Override
	public List<JobDto> readUpcomingJobsForExecution() {
		return select("""
				SELECT *
				FROM job_queue_tbl
				WHERE running <= 0
				  AND nextStart IS NOT NULL
				  AND nextStart < CURRENT_TIMESTAMP
				  AND deleted <= 0
				  AND (lastResult IS NULL OR lastResult = 'OK' OR startAfterError > 0)
				  AND job_name IS NOT NULL
				""", new Job_RowMapper());
	}

	@Override
	public List<JobDto> getJobsWithLostNextStart() {
		return select(isOracleDB() || isPostgreSQL() ? """
			SELECT * FROM job_queue_tbl
			WHERE deleted <= 0
			  AND startAfterError > 0
			  AND TRIM(interval) IS NOT NULL
			  AND nextStart IS NULL
			  AND hostname = ?
			""" : """
			SELECT * FROM job_queue_tbl
			WHERE deleted <= 0
			  AND startAfterError > 0
			  AND TRIM(`interval`) != ''
			  AND nextStart IS NULL
			  AND hostname = ?
			""", new Job_RowMapper(), AgnUtils.getHostName());
	}

	@Override
	public JobDto getJob(int id) {
		return selectObjectDefaultNull("SELECT * FROM job_queue_tbl WHERE id = ?", new Job_RowMapper(), id);
	}

	@Override
	public JobDto getJob(String description) {
		if (StringUtils.isBlank(description)) {
			return null;
		}

		return selectObjectDefaultNull("SELECT * FROM job_queue_tbl WHERE description = ?", new Job_RowMapper(), description);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean initJobStart(int id, Date nextStart) {
		return initJobStart(id, nextStart, false);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean initJobStart(int id, Date nextStart, boolean manuallyOverride) {
		if (id < 0) {
			return false;
		}

		// Lock this job by setting running flag and setting next start to calculated value
		int touchedLines = update("UPDATE job_queue_tbl SET running = 1, nextStart = ?, hostname = ?"
			+ " WHERE running <= 0"
			+ (manuallyOverride ? "": " AND nextStart IS NOT NULL AND nextStart < CURRENT_TIMESTAMP AND deleted <= 0 AND (lastResult IS NULL OR lastResult = 'OK' OR startAfterError > 0)")
			+ " AND id = ?", nextStart, AgnUtils.getHostName(), id);

		return touchedLines == 1;
	}
	
	/**
	 * Update the jobs status only and ignore the optional additional parameters in JOB_QUEUE_PARAM_TBL
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean updateJob(JobDto job) {
		if (job == null) {
			return false;
		}

		// Update Job in DB
		String lastResult = job.getLastResult();
		if (lastResult != null && lastResult.length() > 512) {
			lastResult = lastResult.substring(0, 508) + " ...";
		}
		String updateSql;
		if (isOracleDB() || isPostgreSQL()) {
			updateSql = "UPDATE job_queue_tbl SET description = ?, created = ?, lastStart = ?, running = ?, lastResult = ?" + (lastResult == null || "OK".equalsIgnoreCase(lastResult) ? ", acknowledged = 0" : "") + ", startAfterError = ?, lastDuration = ?, interval = ?, nextStart = ?, hostname = ?, runonlyonhosts = ?, emailonerror = ?, deleted = ? WHERE id = ?";
		} else {
			updateSql = "UPDATE job_queue_tbl SET description = ?, created = ?, lastStart = ?, running = ?, lastResult = ?" + (lastResult == null || "OK".equalsIgnoreCase(lastResult) ? ", acknowledged = 0" : "") + ", startAfterError = ?, lastDuration = ?, `interval` = ?, nextStart = ?, hostname = ?, runonlyonhosts = ?, emailonerror = ?, deleted = ? WHERE id = ?";
		}
		int touchedLines = update(
				updateSql,
				job.getDescription(),
				job.getCreated(),
				job.getLastStart(),
				job.isRunning() ? 1 : 0,
				lastResult,
				job.isStartAfterError() ? 1 : 0,
				job.getLastDuration(),
				job.getInterval(),
				job.getNextStart(),
				AgnUtils.getHostName(),
				job.getRunOnlyOnHosts(),
				job.getEmailOnError(),
				job.isDeleted() ? 1 : 0,
				job.getId()
		);

		if (touchedLines != 1) {
			throw new IllegalStateException("Invalid touched lines amount");
		}

		return true;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void resetJobsForCurrentHost() {
		fixRenamedJobs();
		fixKilledJobs();
		fixErroneousJobs();
		fixMissingNextStartJobs();
	}

	private void fixErroneousJobs() {
		String hostName = AgnUtils.getHostName();
		List<JobDto> jobs = select("""
            SELECT *
            FROM job_queue_tbl
            WHERE hostname = ?
              AND lastresult IS NOT NULL
              AND lastresult != 'OK'""", new Job_RowMapper(), hostName);
		for (JobDto job : jobs) {
			job.setLastResult(null);
			logger.error("Resetting erroneous job ({}) formerly started by host ({})", job, hostName);
			updateJob(job);
		}
	}

	private void fixKilledJobs() {
		String hostName = AgnUtils.getHostName();
		List<JobDto> jobs = select("""
            SELECT *
            FROM job_queue_tbl
            WHERE hostname = ?
              AND running = 1""", new Job_RowMapper(), hostName);
		for (JobDto job : jobs) {
			job.setRunning(false);
			job.setNextStart(new Date());
			logger.error("Resetting killed job ({}) formerly started by host ({})", job, hostName);
			updateJob(job);
		}
	}

	// TODO: Remove after all instances have been updated to version 25.04.373 or later
	private void fixRenamedJobs() {
		String hostName = AgnUtils.getHostName();
		List<JobDto> jobs = select("""
            SELECT *
            FROM job_queue_tbl
            WHERE hostname = ?
                AND nextstart IS NULL
                AND runclass IN (
                'com.agnitas.mailing.autooptimization.service.OptimizationJobWorker',
                'com.agnitas.emm.core.workflow.service.WorkflowReminderServiceJobWorker',
                'com.agnitas.emm.core.birtreport.service.BirtReportJobWorker',
                'com.agnitas.emm.core.calendar.service.CalendarCommentMailingServiceJobWorker',
                'com.agnitas.emm.core.workflow.service.jobs.WorkflowReactionJobWorker',
                'com.agnitas.util.quartz.DBCleanerCompanyCleaningSubWorker',
                'com.agnitas.util.quartz.DBCleanerJobWorker',
                'com.agnitas.util.quartz.DBErrorCheckJobWorker',
                'com.agnitas.util.quartz.FailedTestDeliveryCleanupJobWorker',
                'com.agnitas.util.quartz.LoginTrackTableCleanerJobWorker',
                'com.agnitas.util.quartz.OpenEMMCompanyWorker',
                'com.agnitas.util.quartz.RecipientChartPreCalculatorJobWorker',
                'com.agnitas.util.quartz.RecipientHstCleanerJobWorker',
                'com.agnitas.util.quartz.WebserviceLoginTrackTableCleanerJobWorker',
                'com.agnitas.emm.core.auto_import.worker.AutoImportJobWorker',
                'com.agnitas.emm.core.autoexport.worker.AutoExportJobWorker',
                'com.agnitas.service.RecipientChartJobWorker')""", new Job_RowMapper(), hostName);
		for (JobDto job : jobs) {
			job.setLastResult(null);
			job.setNextStart(new Date());
			logger.error("Resetting renamed job ({}) formerly started by host ({})", job, hostName);
			updateJob(job);
		}
	}

	private void fixMissingNextStartJobs() {
		getJobsWithLostNextStart().forEach(this::tryResetJobNextStart);
	}

	private void tryResetJobNextStart(JobDto job) {
		try {
			job.setNextStart(DateUtilities.calculateNextJobStart(job.getInterval()));
			logger.error("Resetting job with a missing next start ({}) formerly started by host ({})", job, AgnUtils.getHostName());
			updateJob(job);
		} catch (Exception e) {
			logger.error("Cannot calculate next start for the job {}({})", job.getDescription(), job.getId());
		}
	}

	private class Job_RowMapper implements RowMapper<JobDto> {
		@Override
		public JobDto mapRow(ResultSet resultSet, int row) throws SQLException {
			JobDto newJob = new JobDto();

			newJob.setId(resultSet.getInt("id"));
			newJob.setName(resultSet.getString("job_name"));
			newJob.setDescription(resultSet.getString("description"));
			newJob.setCreated(resultSet.getTimestamp("created"));
			newJob.setLastStart(resultSet.getTimestamp("lastStart"));
			newJob.setRunning(resultSet.getInt("running") > 0);
			newJob.setLastResult(resultSet.getString("lastResult"));
			newJob.setStartAfterError(resultSet.getInt("startAfterError") > 0);
			newJob.setLastDuration(resultSet.getInt("lastDuration"));
			newJob.setInterval(resultSet.getString("interval"));
			newJob.setNextStart(resultSet.getTimestamp("nextStart"));
			newJob.setRunClass(resultSet.getString("runClass"));
			newJob.setDeleted(resultSet.getInt("deleted") > 0);
			newJob.setRunOnlyOnHosts(resultSet.getString("runonlyonhosts"));
			newJob.setEmailOnError(resultSet.getString("emailonerror"));
			newJob.setCriticality(resultSet.getInt("criticality"));
			newJob.setAcknowledged(resultSet.getInt("acknowledged") > 0);

			// Read parameters for this job
			List<Map<String, Object>> result = select("SELECT * FROM job_queue_parameter_tbl WHERE job_id = ?", newJob.getId());
			
			Map<String, String> parameters = new HashMap<>();
			for (Map<String, Object> resultRow : result) {
				parameters.put((String) resultRow.get("parameter_name"), (String) resultRow.get("parameter_value"));
			}
			newJob.setParameters(parameters);
			
			return newJob;
		}
	}

	@Override
	public List<JobDto> selectErroneousJobs() {
		if (isOracleDB() || isPostgreSQL()) {
			return select("""
					SELECT *
					FROM job_queue_tbl
					WHERE deleted <= 0
					  AND ((lastResult IS NULL OR lastResult != 'OK') OR (nextStart IS NULL OR nextStart < %s) OR
					       interval IS NULL OR runClass IS NULL OR job_name IS NULL)
					""".formatted(isOracleDB() ? "CURRENT_TIMESTAMP - 0.05" : "CURRENT_TIMESTAMP - (INTERVAL '0.05 MINUTE' * 24 * 60)"),
					new Job_RowMapper());
		}

		return select("""
                SELECT *
                FROM job_queue_tbl
                WHERE deleted <= 0
                  AND ((lastResult IS NULL OR lastResult != 'OK') OR
                       (nextStart IS NULL OR nextStart < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 * 60 * 0.05 MINUTE)) OR
                       `interval` IS NULL OR runClass IS NULL OR job_name IS NULL)
                """, new Job_RowMapper());
	}
	
	@Override
	public List<JobDto> selectCriticalErroneousJobs() {
		if (isOracleDB() || isPostgreSQL()) {
			return select("""
					SELECT *
					FROM job_queue_tbl
					WHERE deleted <= 0
					  AND criticality > 3
					  AND ((lastResult IS NULL OR lastResult != 'OK') OR (nextStart IS NULL OR nextStart < %s) OR
					       interval IS NULL OR runClass IS NULL OR job_name IS NULL)
					""".formatted(isOracleDB() ? "CURRENT_TIMESTAMP - 0.05" : "CURRENT_TIMESTAMP - (INTERVAL '0.05 MINUTE' * 24 * 60)"),
					new Job_RowMapper());
		}

		return select("""
                SELECT *
                FROM job_queue_tbl
                WHERE deleted <= 0
                  AND criticality > 3
                  AND ((lastResult IS NULL OR lastResult != 'OK') OR
                       (nextStart IS NULL OR nextStart < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 * 60 * 0.05 MINUTE)) OR
                       `interval` IS NULL OR runClass IS NULL OR job_name IS NULL)
                """, new Job_RowMapper());
	}

	@Override
	public List<JobDto> getAllActiveJobs() {
		return select("SELECT * FROM job_queue_tbl WHERE deleted <= 0 ORDER BY id", new Job_RowMapper());
	}

	@Override
	public List<JobDto> getOverview(JobQueueOverviewFilter filter) {
		StringBuilder query = new StringBuilder("SELECT * FROM job_queue_tbl");
		List<Object> params = applyOverviewFilter(filter, query);
		query.append(" ORDER BY id");

		return select(query.toString(), new Job_RowMapper(), params.toArray());
	}

	@Override
	public int getCountForOverview() {
		return selectInt("SELECT COUNT(*) FROM job_queue_tbl WHERE deleted <= 0");
	}

	private List<Object> applyOverviewFilter(JobQueueOverviewFilter filter, StringBuilder query) {
		query.append(" WHERE deleted <= 0");
		List<Object> params = new ArrayList<>();

		if (filter.getId() != null) {
			query.append(getPartialSearchFilterWithAnd("id", filter.getId(), params));
		}

		if (filter.getRunning() != null) {
			query.append(" AND running = ?");
			params.add(BooleanUtils.toInteger(filter.getRunning()));
		}

		if (StringUtils.isNotBlank(filter.getName())) {
			query.append(getPartialSearchFilterWithAnd("description"));
			params.add(filter.getName());
		}

		if (filter.getSuccessful() != null) {
			if (filter.getSuccessful()) {
				query.append(" AND lastresult = ?");
			} else {
				query.append(" AND (lastresult IS NULL OR lastresult != ?)");
			}
			params.add("OK");
		}

		query.append(getDateRangeFilterWithAnd("nextstart", filter.getNextStartDate(), params));

		return params;
	}
	
	@Override
	public List<JobDto> getHangingJobs(Date timeLimit) {
		return select("""
				SELECT *
				FROM job_queue_tbl
				WHERE deleted <= 0
				  AND running > 0
				  AND lastStart < ?
				ORDER BY id
				""", new Job_RowMapper(), timeLimit);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void writeJobResult(int jobId, Date time, String result, int durationInSeconds, String hostname) {
		// Watchout: OracleDB counts in bytes of texts not in chars
		if (result != null && result.getBytes(StandardCharsets.UTF_8).length > 512) {
			result = result.substring(0, 450) + " ...";
		}
		update("INSERT INTO job_queue_result_tbl (job_id, time, result, duration, hostname) VALUES (?, ?, ?, ?, ?)", jobId, time, result, durationInSeconds, hostname);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public List<Map<String, Object>> getLastJobResults(int jobId) {
		return select(addRowLimit("""
				SELECT time, result, duration, hostname
				FROM job_queue_result_tbl
				WHERE job_id = ?
				ORDER BY time DESC
				""", 10), jobId);
	}
	
	/**
	 * Update Job Status in DB
	 * Do not update the fields "description", "created", "lastStart", "deleted", "nextstart", "interval", "hostname", "runClass", "job_name", "runonlyonhosts", "startAfterError" and "emailonerror",
	 * because these could be managed via db (manually) for the next job executions and should not be changed by signaling the end of a running job
	 */
	@Override
	public boolean updateJobStatus(JobDto job) {
		if (job == null) {
			return false;
		} else {
			while (true) {
				try {
					String lastResult = job.getLastResult();
					if (lastResult != null && lastResult.length() > 512) {
						// Watch out for german Umlaute in string which count as 2 bytes etc., so make it shorter than 508 chars.
						lastResult = lastResult.substring(0, 500) + " ...";
					}
					int touchedLines = update(
						"UPDATE job_queue_tbl SET running = ?, lastResult = ?" + (lastResult == null || "OK".equalsIgnoreCase(lastResult) ? ", acknowledged = 0" : "") + ", lastDuration = ? WHERE id = ?",
						job.isRunning() ? 1 : 0,
						lastResult,
						job.getLastDuration(),
						job.getId());
					
					if (touchedLines != 1) {
						throw new IllegalStateException("Invalid touched lines amount");
					}

					return true;
				} catch (Exception e) {
					logger.error("Error while updating job job status", e);
					// DO NOT throw any Exception here
					// If there was a problem in updating the job status, this must be logged and retried for unlimited times until success.
					try {
						Thread.sleep(1000 * 60);
					} catch (InterruptedException e1) {
						logger.debug("InterruptedException: {}", e1.getMessage());
					}
				}
			}
		}
	}

	@Override
	public void acknowledgeErroneousJob(int idToAcknowledge) {
		update("UPDATE job_queue_tbl SET acknowledged = 1 WHERE id = ?", idToAcknowledge);
	}
	
	@Override
	public void storeDynamicJobParameter(int jobID, String parameterName, String parameterValue) {
		update("DELETE FROM job_queue_parameter_tbl WHERE job_id = ? AND parameter_name = ?", jobID, parameterName);
		if (StringUtils.isNotBlank(parameterValue)) {
			update("INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) VALUES (?, ?, ?)", jobID, parameterName, parameterValue);
		}
	}

}
