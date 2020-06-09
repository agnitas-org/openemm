/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
	
	private static final String FIELD_CREATED = "created";
	private static final String FIELD_LASTSTART = "lastStart";
	private static final String FIELD_RUNNING = "running";
	private static final String FIELD_LASTRESULT = "lastResult";
	private static final String FIELD_STARTAFTERERROR = "startAfterError";
	private static final String FIELD_LASTDURATION = "lastDuration";
	private static final String FIELD_INTERVAL_ORACLE = "interval";
	private static final String FIELD_INTERVAL_MYSQL = "`interval`";
	private static final String FIELD_NEXTSTART = "nextStart";
	private static final String FIELD_HOSTNAME = "hostname";
	private static final String FIELD_RUNCLASS = "runClass";
	private static final String FIELD_DELETED = "deleted";
	private static final String FIELD_RUNONLYONHOSTS = "runonlyonhosts";
	private static final String FIELD_EMAILONERROR = "emailonerror";
	
	private static final String SELECT_UPCOMING_JOBS = "SELECT * FROM job_queue_tbl WHERE " + FIELD_RUNNING + " <= 0 AND " + FIELD_NEXTSTART + " IS NOT NULL AND " + FIELD_NEXTSTART + " < CURRENT_TIMESTAMP AND " + FIELD_DELETED + " <= 0 AND (" + FIELD_LASTRESULT + " IS NULL OR " + FIELD_LASTRESULT + " = 'OK' OR " + FIELD_STARTAFTERERROR + " > 0) AND " + FIELD_RUNCLASS + " IS NOT NULL";
	private static final String SELECT_NOT_DELETED_JOBS = "SELECT * FROM job_queue_tbl WHERE " + FIELD_DELETED + " <= 0 ORDER BY id";
	private static final String SELECT_ERRORNEOUS_JOBS_ORACLE = "SELECT * FROM job_queue_tbl WHERE " + FIELD_DELETED + " <= 0 AND ((" + FIELD_LASTRESULT + " IS NOT NULL AND " + FIELD_LASTRESULT + " != 'OK') OR (" + FIELD_NEXTSTART + " IS NOT NULL AND " + FIELD_NEXTSTART + " < CURRENT_TIMESTAMP - 0.05) OR " + FIELD_INTERVAL_ORACLE + " IS NULL OR " + FIELD_RUNCLASS + " IS NULL)";
	private static final String SELECT_ERRORNEOUS_JOBS_MYSQL = "SELECT * FROM job_queue_tbl WHERE " + FIELD_DELETED + " <= 0 AND ((" + FIELD_LASTRESULT + " IS NOT NULL AND " + FIELD_LASTRESULT + " != 'OK') OR (" + FIELD_NEXTSTART + " IS NOT NULL AND " + FIELD_NEXTSTART + " < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 * 60 * 0.05 MINUTE)) OR " + FIELD_INTERVAL_MYSQL + " IS NULL OR " + FIELD_RUNCLASS + " IS NULL)";
	private static final String SELECT_BY_ID = "SELECT * FROM job_queue_tbl WHERE id = ?";
	private static final String UPDATE_ORACLE = "UPDATE job_queue_tbl SET description = ?, " + FIELD_CREATED + " = ?, " + FIELD_LASTSTART + " = ?, " + FIELD_RUNNING + " = ?, " + FIELD_LASTRESULT + " = ?, " + FIELD_STARTAFTERERROR + " = ?, " + FIELD_LASTDURATION + " = ?, " + FIELD_INTERVAL_ORACLE + " = ?, " + FIELD_NEXTSTART + " = ?, " + FIELD_HOSTNAME + " = ?," + FIELD_RUNCLASS + " = ?, " + FIELD_RUNONLYONHOSTS + " = ?, " + FIELD_EMAILONERROR + " = ?, " + FIELD_DELETED + " = ? WHERE id = ?";
	private static final String UPDATE_MYSQL = "UPDATE job_queue_tbl SET description = ?, " + FIELD_CREATED + " = ?, " + FIELD_LASTSTART + " = ?, " + FIELD_RUNNING + " = ?, " + FIELD_LASTRESULT + " = ?, " + FIELD_STARTAFTERERROR + " = ?, " + FIELD_LASTDURATION + " = ?, " + FIELD_INTERVAL_MYSQL + " = ?, " + FIELD_NEXTSTART + " = ?, " + FIELD_HOSTNAME + " = ?," + FIELD_RUNCLASS + " = ?, " + FIELD_RUNONLYONHOSTS + " = ?, " + FIELD_EMAILONERROR + " = ?, " + FIELD_DELETED + " = ? WHERE id = ?";
	private static final String SELECT_JOB_STATUS_FOR_UPDATE = "SELECT " + FIELD_RUNNING + " FROM job_queue_tbl WHERE " + FIELD_RUNNING + " <= 0 AND " + FIELD_NEXTSTART + " IS NOT NULL AND " + FIELD_NEXTSTART + " < CURRENT_TIMESTAMP AND " + FIELD_DELETED + " <= 0 AND (" + FIELD_LASTRESULT + " IS NULL OR " + FIELD_LASTRESULT + " = 'OK' OR " + FIELD_STARTAFTERERROR + " > 0) AND id = ? FOR UPDATE";
	private static final String SELECT_JOB_STATUS_FOR_UPDATE_MANUALLY_OVERRIDE = "SELECT " + FIELD_RUNNING + " FROM job_queue_tbl WHERE " + FIELD_RUNNING + " <= 0 AND id = ? FOR UPDATE";
	private static final String UPDATE_JOB_STATUS = "UPDATE job_queue_tbl SET " + FIELD_RUNNING + " = 1, " + FIELD_NEXTSTART + " = ?, " + FIELD_HOSTNAME + " = ? WHERE id = ?";
	
	private static final String DELETE_BY_ID = "UPDATE job_queue_tbl SET " + FIELD_DELETED + " = 1 WHERE " + FIELD_DELETED + " <= 0 AND id = ?";
	
	private static final String PARAMETER_FIELD_JOBID = "job_id";
	private static final String PARAMETER_FIELD_PARAMETERNAME = "parameter_name";
	private static final String PARAMETER_FIELD_PARAMETERVALUE = "parameter_value";
	
	private static final String SELECT_PARAMETERS_BY_JOBID = "SELECT * FROM job_queue_parameter_tbl WHERE " + PARAMETER_FIELD_JOBID + " = ?";

	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic

	@Override
	public List<JobDto> readUpcomingJobsForExecution() {
		try {
			return select(logger, SELECT_UPCOMING_JOBS, new Job_RowMapper());
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
				return selectObjectDefaultNull(logger, SELECT_BY_ID, new Job_RowMapper(), id);
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
	public boolean initJobStart(int id, Date nextStart, boolean manuallyOverride) {
		if (id < 0) {
			return false;
		} else {
			try {
				try (Connection connection = getDataSource().getConnection()) {
					boolean previousAutoCommit = connection.getAutoCommit();
					connection.setAutoCommit(false);
					
					String selectSql;
					if (manuallyOverride) {
						selectSql = SELECT_JOB_STATUS_FOR_UPDATE_MANUALLY_OVERRIDE;
					} else {
						selectSql = SELECT_JOB_STATUS_FOR_UPDATE;
					}
					
					logSqlStatement(logger, selectSql);
					try (PreparedStatement lockStatement = connection.prepareStatement(selectSql)) {
						lockStatement.setInt(1, id);
						
						try (ResultSet lockQueryResult = lockStatement.executeQuery()) {
							if (lockQueryResult.next()) {
								// Lock this job by setting to running and calculating next start
								logSqlStatement(logger, UPDATE_JOB_STATUS);
								try(PreparedStatement updateStatement = connection.prepareStatement(UPDATE_JOB_STATUS)) {
									if (nextStart == null) {
										updateStatement.setTimestamp(1, null);
									} else {
										updateStatement.setTimestamp(1, new Timestamp(nextStart.getTime()));
									}
									
									updateStatement.setString(2, AgnUtils.getHostName());
									updateStatement.setInt(3, id);
									updateStatement.executeUpdate();
								}
								
								return true;
							} else {
								return false;
							}
						} finally {
							connection.commit();
						}
					} finally {
						connection.setAutoCommit(previousAutoCommit);
					}
				}
			} catch(Exception e) {
				logger.error("Error while setting job job status", e);
				throw new RuntimeException("Error while setting job status", e);
			}
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
				int touchedLines = update(logger,
					isOracleDB() ? UPDATE_ORACLE : UPDATE_MYSQL,
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
			if (logger.isDebugEnabled()) {
				logger.debug("stmt:" + DELETE_BY_ID);
			}
			try {
				return update(logger, DELETE_BY_ID, id) > 0;
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
			"UPDATE job_queue_tbl"
			+ " SET " + FIELD_RUNNING + " = 0, " + FIELD_NEXTSTART + " = CURRENT_TIMESTAMP"
			+ " WHERE " + FIELD_HOSTNAME + " = ? AND " + FIELD_RUNNING + " = 1",
			AgnUtils.getHostName());
	}

	private class Job_RowMapper implements RowMapper<JobDto> {
		@Override
		public JobDto mapRow(ResultSet resultSet, int row) throws SQLException {
			JobDto newJob = new JobDto();

			newJob.setId(resultSet.getInt("id"));
			newJob.setDescription(resultSet.getString("description"));
			newJob.setCreated(resultSet.getTimestamp(FIELD_CREATED));
			newJob.setLastStart(resultSet.getTimestamp(FIELD_LASTSTART));
			newJob.setRunning(resultSet.getInt(FIELD_RUNNING) > 0);
			newJob.setLastResult(resultSet.getString(FIELD_LASTRESULT));
			newJob.setStartAfterError(resultSet.getInt(FIELD_STARTAFTERERROR) > 0);
			newJob.setLastDuration(resultSet.getInt(FIELD_LASTDURATION));
			newJob.setInterval(resultSet.getString(FIELD_INTERVAL_ORACLE));
			newJob.setNextStart(resultSet.getTimestamp(FIELD_NEXTSTART));
			newJob.setRunClass(resultSet.getString(FIELD_RUNCLASS));
			newJob.setDeleted(resultSet.getInt(FIELD_DELETED) > 0);
			newJob.setRunOnlyOnHosts(resultSet.getString(FIELD_RUNONLYONHOSTS));
			newJob.setEmailOnError(resultSet.getString(FIELD_EMAILONERROR));

			// Read parameters for this job
			if (logger.isDebugEnabled()) {
				logger.debug("stmt:" + SELECT_PARAMETERS_BY_JOBID);
			}
			List<Map<String, Object>> result = select(logger, SELECT_PARAMETERS_BY_JOBID, newJob.getId());
			
			Map<String, String> parameters = new HashMap<>();
			for (Map<String, Object> resultRow : result) {
				parameters.put((String) resultRow.get(PARAMETER_FIELD_PARAMETERNAME), (String) resultRow.get(PARAMETER_FIELD_PARAMETERVALUE));
			}
			newJob.setParameters(parameters);
			
			return newJob;
		}
	}

	@Override
	public List<JobDto> selectErrorneousJobs() {
		try {
			if (isOracleDB()) {
				return select(logger, SELECT_ERRORNEOUS_JOBS_ORACLE, new Job_RowMapper());
			} else {
				return select(logger, SELECT_ERRORNEOUS_JOBS_MYSQL, new Job_RowMapper());
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while reading errorneous jobs from database", e);
		}
	}

	@Override
	public List<JobDto> getAllActiveJobs() {
		try {
			return select(logger, SELECT_NOT_DELETED_JOBS, new Job_RowMapper());
		} catch (Exception e) {
			throw new RuntimeException("Error while reading not deleted jobs from database", e);
		}
	}
	
	@Override
	public List<JobDto> getHangingJobs(Date timeLimit) {
		try {
			return select(logger, "SELECT * FROM job_queue_tbl WHERE " + FIELD_DELETED + " <= 0 AND " + FIELD_RUNNING + " > 0 AND " + FIELD_LASTSTART + " < ? ORDER BY id", new Job_RowMapper(), timeLimit);
		} catch (Exception e) {
			throw new RuntimeException("Error while reading hanging jobs from database", e);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void writeJobResult(int job_id, Date time, String result, int durationInSeconds, String hostname) {
		try {
			if (result != null && result.length() > 512) {
				result = result.substring(0, 508) + " ...";
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
					lastResult = lastResult.substring(0, 508) + " ...";
				}
				int touchedLines = update(logger,
					"UPDATE job_queue_tbl SET running = ?, lastResult = ?, lastDuration = ? WHERE id = ?",
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
}
