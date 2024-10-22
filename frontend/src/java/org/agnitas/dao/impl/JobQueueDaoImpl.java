/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.serverstatus.forms.JobQueueOverviewFilter;
import org.agnitas.dao.JobQueueDao;
import org.agnitas.service.JobDto;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO handler for JobDto-Objects
 */
public class JobQueueDaoImpl extends BaseDaoImpl implements JobQueueDao {

	private static final Logger logger = LogManager.getLogger(JobQueueDaoImpl.class);
	
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
		int killedJobsResetted = update(logger,
			"UPDATE job_queue_tbl SET running = 0, nextStart = CURRENT_TIMESTAMP WHERE hostname = ? AND running = 1",
			AgnUtils.getHostName());
		int errorneousJobsResetted = update(logger,
			"UPDATE job_queue_tbl SET lastresult = NULL WHERE hostname = ? AND (lastresult IS NOT NULL AND lastresult != 'OK')",
			AgnUtils.getHostName());
		return killedJobsResetted + errorneousJobsResetted;
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
	public List<JobDto> selectErroneousJobs() {
		try {
			if (isOracleDB()) {
				return select(logger, "SELECT * FROM job_queue_tbl WHERE deleted <= 0 AND ((lastResult IS NOT NULL AND lastResult != 'OK') OR (nextStart IS NOT NULL AND nextStart < CURRENT_TIMESTAMP - 0.05) OR interval IS NULL OR runClass IS NULL)", new Job_RowMapper());
			} else {
				return select(logger, "SELECT * FROM job_queue_tbl WHERE deleted <= 0 AND ((lastResult IS NOT NULL AND lastResult != 'OK') OR (nextStart IS NOT NULL AND nextStart < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 * 60 * 0.05 MINUTE)) OR `interval` IS NULL OR runClass IS NULL)", new Job_RowMapper());
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while reading erroneous jobs from database", e);
		}
	}
	
	@Override
	public List<JobDto> selectCriticalErroneousJobs() {
		try {
			if (isOracleDB()) {
				return select(logger, "SELECT * FROM job_queue_tbl WHERE deleted <= 0 AND criticality > 3 AND ((lastResult IS NOT NULL AND lastResult != 'OK') OR (nextStart IS NOT NULL AND nextStart < CURRENT_TIMESTAMP - 0.05) OR interval IS NULL OR runClass IS NULL)", new Job_RowMapper());
			} else {
				return select(logger, "SELECT * FROM job_queue_tbl WHERE deleted <= 0 AND criticality > 3 AND ((lastResult IS NOT NULL AND lastResult != 'OK') OR (nextStart IS NOT NULL AND nextStart < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 * 60 * 0.05 MINUTE)) OR `interval` IS NULL OR runClass IS NULL)", new Job_RowMapper());
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while reading erroneous jobs from database", e);
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
	public List<JobDto> getOverview(JobQueueOverviewFilter filter) {
		StringBuilder query = new StringBuilder("SELECT * FROM job_queue_tbl");
		List<Object> params = applyOverviewFilter(filter, query);
		query.append(" ORDER BY id");

		return select(logger, query.toString(), new Job_RowMapper(), params.toArray());
	}

	@Override
	public int getCountForOverview() {
		return selectInt(logger, "SELECT COUNT(*) FROM job_queue_tbl WHERE deleted <= 0");
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

		if (filter.getStartDate().getFrom() != null) {
			query.append(" AND nextstart >= ?");
			params.add(filter.getStartDate().getFrom());
		}
		if (filter.getStartDate().getTo() != null) {
			query.append(" AND nextstart < ?");
			params.add(DateUtilities.addDaysToDate(filter.getStartDate().getTo(), 1));
		}

		return params;
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
	@DaoUpdateReturnValueCheck
	public List<Map<String, Object>> getLastJobResults(int job_id) {
		try {
			if (isOracleDB()) {
				return select(logger, "SELECT * FROM (SELECT time, result, duration, hostname FROM job_queue_result_tbl WHERE job_id = ? ORDER BY time DESC) WHERE rownum <= 10", job_id);
			} else {
				return select(logger, "SELECT time, result, duration, hostname FROM job_queue_result_tbl WHERE job_id = ? ORDER BY time DESC LIMIT 10", job_id);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while writing JobResult", e);
		}
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
			while (true) {
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
					// DO NOT throw any Exception here
					// If there was a problem in updating the job status, this must be logged and retried for unlimited times until success.
					try {
						Thread.sleep(1000 * 60);
					} catch (InterruptedException e1) {
						if (logger.isDebugEnabled()) {
							logger.debug("InterruptedException: " + e1.getMessage());
						}
					}
				}
			}
		}
	}

	@Override
	public void acknowledgeErroneousJob(int idToAcknowledge) {
		update(logger, "UPDATE job_queue_tbl SET acknowledged = 1 WHERE id = ?", idToAcknowledge);
	}
	
	@Override
	public void storeDynamicJobParameter(int jobID, String parameterName, String parameterValue) {
		update(logger, "DELETE FROM job_queue_parameter_tbl WHERE job_id = ? AND parameter_name = ?", jobID, parameterName);
		if (StringUtils.isNotBlank(parameterValue)) {
			update(logger, "INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) VALUES (?, ?, ?)", jobID, parameterName, parameterValue);
		}
	}
}
