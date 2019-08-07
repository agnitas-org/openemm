/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mailing.bean.ComFollowUpStats;
import com.agnitas.emm.core.mailing.dao.ComFollowUpStatsDao;

public class ComFollowUpStatsDaoImpl extends BaseDaoImpl implements ComFollowUpStatsDao {
	private static final transient Logger logger = Logger.getLogger(ComFollowUpStatsDaoImpl.class);
	
	@Override
	public ComFollowUpStats getStatEntry(int followUpID, String sessionID) {
		String sql = "SELECT result_id, company_id, basemail_id, followup_id, creation_date, duration_time, session_id, statement, result_value"
				+ " FROM followup_stat_result_tbl"
				+ " WHERE followup_id = ? AND session_id = ? AND result_id IN (SELECT MAX(result_id) FROM followup_stat_result_tbl WHERE followup_id = ? AND session_id = ?)";
		try { 
			ComFollowUpStats followUpStats =  selectObjectDefaultNull(logger, sql, new FollowupRowMapper(), followUpID, sessionID, followUpID, sessionID);
			if (followUpStats == null && logger.isInfoEnabled()) {
				logger.info("getStatEntry found no entry, so no result ist given back.");
			}
			return followUpStats;
		} catch (Exception e) {
			logger.error("Error getting FollowupStatEntry: " + e);
			return null;
		}
	}

	@Override
	public ComFollowUpStats getStatEntry(int resultID) {
		String sql = "SELECT result_id, company_id, basemail_id, followup_id, creation_date, duration_time, session_id, statement, result_value FROM followup_stat_result_tbl WHERE result_id = ?";
		return selectObjectDefaultNull(logger, sql, new FollowupRowMapper(), resultID);	
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int createNewStatEntry(@VelocityCheck int companyID,int basemailID, int followupID, String sessionID, String statement) {
		if (isOracleDB()) {
			int resultID = selectInt(logger, "SELECT followup_stat_result_seq.NEXTVAL FROM DUAL");

			String insertSql = "INSERT INTO followup_stat_result_tbl (result_id, company_id, basemail_id, followup_id, creation_date, duration_time, session_id, statement, result_value) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 0, ?, ?, 0)";
			try {
				update(logger, insertSql, resultID, companyID, basemailID, followupID, sessionID, statement);
				return resultID;
			} catch (Exception e) {
				logger.error("Error inserting new FollowUpStat Entry with companyID: " + companyID + " SessionID: " + sessionID + " SQL: " + statement);
				return 0;
			}
        } else {
			String insertSql = "INSERT INTO followup_stat_result_tbl (company_id, basemail_id, followup_id, creation_date, duration_time, session_id, statement, result_value) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 0, ?, ?, 0)";
			try {
				int resultID = insertIntoAutoincrementMysqlTable(logger, "result_id", insertSql, companyID, basemailID, followupID, sessionID, statement);
				return resultID;
			} catch (Exception e) {
				logger.error("Error inserting new FollowUpStat Entry with companyID: " + companyID + " SessionID: " + sessionID + " SQL: " + statement);
				return 0;
			}
        }
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateStatEntry(int resultID, long duration, int result) {
		update(logger, "UPDATE followup_stat_result_tbl SET duration_time = ? WHERE result_id = ?", duration, resultID);
		update(logger, "UPDATE followup_stat_result_tbl SET result_value = ? WHERE result_id = ?", result, resultID);
	}
	
	@Override
	public boolean deleteByCompany(@VelocityCheck int companyID) {
		update(logger, "DELETE FROM followup_stat_result_tbl WHERE company_id = ?", companyID);
		return selectInt(logger, "SELECT COUNT(*) FROM followup_stat_result_tbl WHERE company_id = ?", companyID) == 0;
	}
	
	protected class FollowupRowMapper implements RowMapper<ComFollowUpStats> {
		@Override
		public ComFollowUpStats mapRow(ResultSet rs, int rowNum) throws SQLException {
			ComFollowUpStats followUpStats = new ComFollowUpStats();
			followUpStats.setResultID(Integer.parseInt(rs.getString("result_ID")));
			followUpStats.setCompanyID(Integer.parseInt(rs.getString("company_ID")));	
			followUpStats.setFollowupID(Integer.parseInt(rs.getString("followup_ID")));
			followUpStats.setBasemailID(Integer.parseInt(rs.getString("basemail_ID")));
			followUpStats.setCreationDate(Timestamp.valueOf(rs.getString("creation_date")));
			followUpStats.setDuration(Integer.parseInt(rs.getString("duration_time")));
			followUpStats.setSessionID(rs.getString("session_id"));
			followUpStats.setResultValue(Integer.parseInt(rs.getString("result_value")));
			return followUpStats;			
		}		
	}
}
