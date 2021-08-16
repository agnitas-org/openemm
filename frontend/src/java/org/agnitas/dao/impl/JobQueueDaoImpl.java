/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.dao.JobQueueDao;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.service.JobDto;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

/**
 * DAO handler for JobDto-Objects
 */
public class JobQueueDaoImpl extends BaseDaoImpl implements JobQueueDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(JobQueueDaoImpl.class);
	
	@Override
	public List<JobDto> readUpcomingJobsForExecution() {
		try {
			return select(logger, "SELECT * FROM job_queue_tbl WHERE running <= 0 AND nextStart IS NOT NULL AND nextStart < CURRENT_TIMESTAMP AND deleted <= 0 AND (lastResult IS NULL OR lastResult = 'OK' OR startAfterError > 0) AND runClass IS NOT NULL", new Job_RowMapper());
		} catch (Exception e) {
			throw new RuntimeException("Error while reading jobs from database", e);
		}
	}

	@Override
	public JobDto getJob(int id) {
		if (id <= 0) {
			return null;
		} else {
			try {
				return selectObjectDefaultNull(logger, "SELECT * FROM job_queue_tbl WHERE id = ?", new Job_RowMapper(), id);
			} catch (Exception e) {
				throw new RuntimeException("Error while reading job from database", e);
			}
		}
	}

	@Override
	public JobDto getJob(String description) {
		if (StringUtils.isBlank(description)) {
			return null;
		} else {
			try {
				return selectObjectDefaultNull(logger, "SELECT * FROM job_queue_tbl WHERE description = ?", new Job_RowMapper(), description);
			} catch (Exception e) {
				throw new RuntimeException("Error while reading job from database", e);
			}
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean initJobStart(int id, Date nextStart) {
		return initJobStart(id, nextStart, false);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean initJobStart(final int id, final Date nextStart, final boolean manuallyOverride) {
		if (id < 0) {
			return false;
		} else {
			// Lock this job by setting running flag and setting next start to calculated value
			int touchedLines = update(logger, "UPDATE job_queue_tbl SET running = 1, nextStart = ?, hostname = ?"
				+ " WHERE running <= 0"
				+ (manuallyOverride ? "": " AND nextStart IS NOT NULL AND nextStart < CURRENT_TIMESTAMP AND deleted <= 0 AND (lastResult IS NULL OR lastResult = 'OK' OR startAfterError > 0)")
				+ " AND id = ?", nextStart, AgnUtils.getHostName(), id);
			
			return touchedLines == 1;
		}
	}
	
	/**
	 * Update the jobs status only and ignore the optional additional parameters in JOB_QUEUE_PARAM_TBL
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean updateJob(JobDto job) {
		if (job == null) {
			return false;
		} else {
			try {
				// Update Job in DB
				String lastResult = job.getLastResult();
				if (lastResult != null && lastResult.length() > 512) {
					lastResult = lastResult.substring(0, 508) + " ...";
				}
				String updateSql;
				if (isOracleDB()) {
					updateSql = "UPDATE job_queue_tbl SET description = ?, created = ?, lastStart = ?, running = ?, lastResult = ?" + (lastResult == null || "OK".equalsIgnoreCase(lastResult) ? ", acknowledged = 0" : "") + ", startAfterError = ?, lastDuration = ?, interval = ?, nextStart = ?, hostname = ?, runClass = ?, runonlyonhosts = ?, emailonerror = ?, deleted = ? WHERE id = ?";
				} else {
					updateSql = "UPDATE job_queue_tbl SET description = ?, created = ?, lastStart = ?, running = ?, lastResult = ?" + (lastResult == null || "OK".equalsIgnoreCase(lastResult) ? ", acknowledged = 0" : "") + ", startAfterError = ?, lastDuration = ?, `interval` = ?, nextStart = ?, hostname = ?, runClass = ?, runonlyonhosts = ?, emailonerror = ?, deleted = ? WHERE id = ?";
				}
				int touchedLines = update(logger,
					updateSql,
					job.getDescription(),
					job.getCreated(),
					job.getLastStart(),
					job.isRunning(),
					lastResult,
					job.isStartAfterError(),
					job.getLastDuration(),
					job.getInterval(),
					job.getNextStart(),
					AgnUtils.getHostName(),
					job.getRunClass(),
					job.getRunOnlyOnHosts(),
					job.getEmailOnError(),
					job.isDeleted(),
					job.getId());
				
				if (touchedLines != 1) {
					throw new RuntimeException("Invalid touched lines amount");
				} else {
					return true;
				}
			} catch (Exception e) {
				logger.error("Error while updating job job status", e);
				throw new RuntimeException("Error while updating job job status", e);
			}
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteJob(int id) {
		if (id <= 0) {
			return false;
		} else {
			try {
				return update(logger, "UPDATE job_queue_tbl SET deleted = 1 WHERE deleted <= 0 AND id = ?", id) > 0;
			} catch (DataAccessException e) {
				// No Job found
				return false;
			} catch (Exception e) {
				throw new RuntimeException("Error while deleting job from database", e);
			}
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int resetJobsForCurrentHost() {
		return update(logger,
			"UPDATE job_queue_tbl SET running = 0, nextStart = CURRENT_TIMESTAMP WHERE hostname = ? AND running = 1",
			AgnUtils.getHostName());
	}

	private class Job_RowMapper implements RowMapper<JobDto> {
		@Override
		public JobDto mapRow(ResultSet resultSet, int row) throws SQLException {
			JobDto newJob = new JobDto();

			newJob.setId(resultSet.getInt("id"));
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
			if (logger.isDebugEnabled()) {
				logger.debug("stmt:" + "SELECT * FROM job_queue_parameter_tbl WHERE job_id = ?");
			}
			List<Map<String, Object>> result = select(logger, "SELECT * FROM job_queue_parameter_tbl WHERE job_id = ?", newJob.getId());
			
			Map<String, String> parameters = new HashMap<>();
			for (Map<String, Object> resultRow : result) {
				parameters.put((String) resultRow.get("parameter_name"), (String) resultRow.get("parameter_value"));
			}
			newJob.setParameters(parameters);
			
			return newJob;
		}
	}

	@Override
	public List<JobDto> selectErrorneousJobs() {
		try {
			if (isOracleDB()) {
				return select(logger, "SELECT * FROM job_queue_tbl WHERE deleted <= 0 AND ((lastResult IS NOT NULL AND lastResult != 'OK') OR (nextStart IS NOT NULL AND nextStart < CURRENT_TIMESTAMP - 0.05) OR interval IS NULL OR runClass IS NULL)", new Job_RowMapper());
			} else {
				return select(logger, "SELECT * FROM job_queue_tbl WHERE deleted <= 0 AND ((lastResult IS NOT NULL AND lastResult != 'OK') OR (nextStart IS NOT NULL AND nextStart < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 * 60 * 0.05 MINUTE)) OR `interval` IS NULL OR runClass IS NULL)", new Job_RowMapper());
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while reading errorneous jobs from database", e);
		}
	}

	@Override
	public List<JobDto> getAllActiveJobs() {
		try {
			return select(logger, "SELECT * FROM job_queue_tbl WHERE deleted <= 0 ORDER BY id", new Job_RowMapper());
		} catch (Exception e) {
			throw new RuntimeException("Error while reading not deleted jobs from database", e);
		}
	}
	
	@Override
	public List<JobDto> getHangingJobs(Date timeLimit) {
		try {
			return select(logger, "SELECT * FROM job_queue_tbl WHERE deleted <= 0 AND running > 0 AND lastStart < ? ORDER BY id", new Job_RowMapper(), timeLimit);
		} catch (Exception e) {
			throw new RuntimeException("Error while reading hanging jobs from database", e);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void writeJobResult(int job_id, Date time, String result, int durationInSeconds, String hostname) {
		try {
			// Watchout: OracleDB counts in bytes of texts not in chars
			if (result != null && result.getBytes("UTF-8").length > 512) {
				result = result.substring(0, 450) + " ...";
			}
			update(logger, "INSERT INTO job_queue_result_tbl (job_id, time, result, duration, hostname) VALUES (?, ?, ?, ?, ?)", job_id, time, result, durationInSeconds, hostname);
		} catch (Exception e) {
			throw new RuntimeException("Error while writing JobResult", e);
		}
	}
	
	@Override
	public boolean setStartCompanyForNextCleanupStart(int currentCompanyID) {
		try {
			return update(logger, "INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) (SELECT id, 'startcompany', ? FROM job_queue_tbl WHERE runclass = 'org.agnitas.util.quartz.DBCleanerJobWorker')", currentCompanyID) == 1;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public int getStartCompanyForCleanup() {
		String result = selectObjectDefaultNull(logger, "SELECT parameter_value FROM job_queue_parameter_tbl WHERE parameter_name = 'startcompany' AND job_id = (SELECT id FROM job_queue_tbl WHERE runclass = 'org.agnitas.util.quartz.DBCleanerJobWorker')", new StringRowMapper());
		if (StringUtils.isNotEmpty(result)) {
			return Integer.parseInt(result);
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean deleteCleanupStartEntry() {
		try {
			return update(logger, "DELETE FROM job_queue_parameter_tbl WHERE parameter_name = 'startcompany' AND job_id = (SELECT id FROM job_queue_tbl WHERE runclass = 'org.agnitas.util.quartz.DBCleanerJobWorker')") == 1;
		} catch (Exception e) {
			return false;
		}
	}

	protected String getTargetTempPrefix() {
		return AgnUtils.getRandomString(10);
	}

	/**
	 * Update Job Status in DB
	 * Do not update the fields "description", "created", "lastStart", "deleted", "nextstart", "interval", "hostname", "runClass", "runonlyonhosts", "startAfterError" and "emailonerror",
	 * because these could be managed via db (manually) for the next job executions and should not be changed by signaling the end of a running job
	 */
	@Override
	public boolean updateJobStatus(JobDto job) {
		if (job == null) {
			return false;
		} else {
			try {
				String lastResult = job.getLastResult();
				if (lastResult != null && lastResult.length() > 512) {
					// Watch out for german Umlaute in string which count as 2 bytes etc., so make it shorter than 508 chars.
					lastResult = lastResult.substring(0, 500) + " ...";
				}
				int touchedLines = update(logger,
					"UPDATE job_queue_tbl SET running = ?, lastResult = ?" + (lastResult == null || "OK".equalsIgnoreCase(lastResult) ? ", acknowledged = 0" : "") + ", lastDuration = ? WHERE id = ?",
					job.isRunning(),
					lastResult,
					job.getLastDuration(),
					job.getId());
				
				if (touchedLines != 1) {
					throw new RuntimeException("Invalid touched lines amount");
				} else {
					return true;
				}
			} catch (Exception e) {
				logger.error("Error while updating job job status", e);
				throw new RuntimeException("Error while updating job job status", e);
			}
		}
	}

	@Override
	public void acknowledgeErrorneousJob(int idToAcknowledge) {
		update(logger, "UPDATE job_queue_tbl SET acknowledged = 1 WHERE id = ?", idToAcknowledge);
	}
}
