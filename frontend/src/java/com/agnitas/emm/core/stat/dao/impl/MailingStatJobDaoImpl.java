/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.stat.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.stat.beans.MailingStatJobDescriptor;
import com.agnitas.emm.core.stat.dao.MailingStatJobDao;

public class MailingStatJobDaoImpl extends BaseDaoImpl implements MailingStatJobDao {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(MailingStatJobDaoImpl.class);

	@Override
	@DaoUpdateReturnValueCheck
	public int createMailingStatJob(MailingStatJobDescriptor job) {
		int newId = 0;
		if (isOracleDB()) {
			newId = selectInt(logger, "SELECT mailing_stat_job_tbl_seq.NEXTVAL FROM dual");
			update(logger, "INSERT INTO mailing_statistic_job_tbl (mailing_stat_job_id, job_status, mailing_id, target_groups, recipients_type, change_date, job_status_descr) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)",
						newId, job.getStatus(), job.getMailingId(), job.getTargetGroups(), job.getRecipientsType(), job.getStatusDescription());
        } else {
        	String insertStatement = "INSERT INTO mailing_statistic_job_tbl (job_status, mailing_id, target_groups, recipients_type, change_date, job_status_descr) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

	        Object[] values = new Object[] { job.getStatus(), job.getMailingId(), job.getTargetGroups(), job.getRecipientsType(), job.getStatusDescription() };

	        newId = insertIntoAutoincrementMysqlTable(logger, "mailing_stat_job_id", insertStatement, values);
		}
		return newId;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateMailingStatJob(int id, int status, String statusDescription) {
		String descr = statusDescription.length() < 4000 ? statusDescription: statusDescription.substring(0, 4000);
		update(logger, "UPDATE mailing_statistic_job_tbl SET job_status = ?, change_date = CURRENT_TIMESTAMP, job_status_descr = ? WHERE mailing_stat_job_id = ?", status, descr, id);
	}

	@Override
	public MailingStatJobDescriptor getMailingStatJob(int id) {
		return selectObject(logger, "SELECT * FROM mailing_statistic_job_tbl WHERE mailing_stat_job_id = ?",
				new MailingStatJobRowMapper(), id);
	}

	@Override
	public List<MailingStatJobDescriptor> findMailingStatJobs(int mailingId,
			int recipientsType, String targetGroups, int maxAgeSeconds) {
		
		if (isOracleDB()) {
			String tgComd = (targetGroups == null || targetGroups.isEmpty()) ? "(target_groups like ? or target_groups is null)" : "target_groups like ?";
			return select(logger, "SELECT * FROM mailing_statistic_job_tbl WHERE mailing_id = ? AND " + tgComd +
					" AND recipients_type = ? AND creation_date > SYSDATE - ?/24/60/60 ORDER BY creation_date DESC",
						new MailingStatJobRowMapper(), mailingId, targetGroups, recipientsType, maxAgeSeconds);
		} else {
			return select(logger, "SELECT * FROM mailing_statistic_job_tbl WHERE mailing_id = ? AND target_groups = ?" +
				" AND recipients_type = ? AND creation_date > (NOW() - INTERVAL ? SECOND) ORDER BY creation_date DESC",
					new MailingStatJobRowMapper(), mailingId, targetGroups, recipientsType, maxAgeSeconds);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public final void removeExpiredMailingStatJobs(final ZonedDateTime threshold) {
		final Date thresholdDate = Date.from(threshold.toInstant());
		
		update(logger, "DELETE FROM mailing_statistic_job_tbl WHERE creation_date < ?", thresholdDate);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteMailingStatJob(int id) {
		update(logger, "DELETE FROM mailing_statistic_job_tbl WHERE mailing_stat_job_id = ?", id);
	}

    private class MailingStatJobRowMapper implements RowMapper<MailingStatJobDescriptor> {
		@Override
		public MailingStatJobDescriptor mapRow(ResultSet resultSet, int row) throws SQLException {
			MailingStatJobDescriptor job = new MailingStatJobDescriptor();
			job.setId(resultSet.getBigDecimal("mailing_stat_job_id").intValue());
			job.setStatus(resultSet.getInt("job_status"));
			job.setMailingId(resultSet.getInt("mailing_id"));
			job.setTargetGroups(resultSet.getString("target_groups"));
			job.setRecipientsType(resultSet.getInt("recipients_type"));
			job.setCreationDate(resultSet.getTimestamp("creation_date"));
			job.setChangeDate(resultSet.getTimestamp("change_date"));
			job.setStatusDescription(resultSet.getString("job_status_descr"));
			return job;
		}
	}

}
