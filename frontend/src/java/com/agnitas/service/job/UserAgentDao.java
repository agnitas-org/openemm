/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.ComUserAgent;

public class UserAgentDao extends BaseDaoImpl {
	private static final transient Logger logger = Logger.getLogger(UserAgentDao.class);

	@DaoUpdateReturnValueCheck
	public void traceAgent(String userAgent) {
		if (userAgent != null) {
			// Try to update an existing useragent entry. This is the standard case which should update 1 entry only
			int touchedLines = update(logger, "UPDATE user_agent_tbl SET change_date = CURRENT_TIMESTAMP, req_counter = req_counter + 1 WHERE user_agent = ?", userAgent);
			
			if (touchedLines == 0) {
				// If nothing was updated, try to insert this new useragent, what can go wrong, if meanwhile the useragent was inserted by another process.
				// More than 1 entry cannot be updated here because useragent is an unique key in this table.
				try {
					if (isOracleDB()) {
						int newId = selectInt(logger, "SELECT user_agent_tbl_seq.NEXTVAL FROM DUAL");
						update(logger, "INSERT INTO user_agent_tbl (user_agent_id, user_agent, req_counter, creation_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", newId, userAgent, 0);
			        } else {
						update(logger, "INSERT INTO user_agent_tbl (user_agent, req_counter, creation_date) VALUES (?, ?, CURRENT_TIMESTAMP)", userAgent, 0);
					}
				} catch (DataIntegrityViolationException e) {
					// do nothing, because meanwhile the useragent was inserted by another process.
				}
				
				// Update the existing useragent entry, which must exist because this process or another process created by now
				touchedLines = update(logger, "UPDATE user_agent_tbl SET change_date = CURRENT_TIMESTAMP, req_counter = req_counter + 1 WHERE user_agent = ?", userAgent);
				if (touchedLines > 1) {
					// Too many rows so remove duplicates. This may only happen if there is no unique key on user_agent
					if (isOracleDB()) {
						update(logger, "DELETE FROM user_agent_tbl a WHERE a.user_agent = ? AND a.user_agent_id > (SELECT MIN(user_agent_id) FROM user_agent_tbl b WHERE b.user_agent = a.user_agent);", userAgent);
					} else {
						int minId = selectInt(logger, "SELECT MIN(user_agent_id) FROM user_agent_tbl WHERE user_agent = ?", userAgent);
						update(logger, "DELETE FROM user_agent_tbl WHERE user_agent = ? AND user_agent_id > ?", userAgent, minId);
					}
				} else if (touchedLines < 1) {
					logger.error("Error writing UserAgent: " + userAgent);
				}
			}
		}
	}
	
	@DaoUpdateReturnValueCheck
	public void traceAgentForClient(String userAgent, int amount) {
		if (userAgent != null) {
			// Try to update an existing useragent entry. This is the standard case which should update 1 entry only
			int touchedLines = update(logger, "UPDATE user_agent_for_client_tbl SET change_date = CURRENT_TIMESTAMP, req_counter = req_counter + ? WHERE user_agent = ?", amount, userAgent);
			
			if (touchedLines == 0) {
				// If nothing was updated, try to insert this new useragent, what can go wrong, if meanwhile the useragent was inserted by another process.
				// More than 1 entry cannot be updated here because useragent is an unique key in this table.
				try {
					if (isOracleDB()) {
						int newId = selectInt(logger, "SELECT user_agent_for_client_tbl_seq.NEXTVAL FROM DUAL");
						update(logger, "INSERT INTO user_agent_for_client_tbl (user_agent_id, user_agent, req_counter, creation_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", newId, userAgent, 0);
			        } else {
						update(logger, "INSERT INTO user_agent_for_client_tbl (user_agent, req_counter, creation_date) VALUES (?, ?, CURRENT_TIMESTAMP)", userAgent, 0);
					}
				} catch (DataIntegrityViolationException e) {
					// do nothing, because meanwhile the useragent was inserted by another process.
				}
				
				// Update the existing useragent entry, which must exist because this process or another process created by now
				touchedLines = update(logger, "UPDATE user_agent_for_client_tbl SET change_date = CURRENT_TIMESTAMP, req_counter = req_counter + ? WHERE user_agent = ?", amount, userAgent);
				if (touchedLines > 1) {
					// Too many rows so remove duplicates. This may only happen if there is no unique key on user_agent
					if (isOracleDB()) {
						update(logger, "DELETE FROM user_agent_for_client_tbl a WHERE a.user_agent = ? AND a.user_agent_id > (SELECT MIN(user_agent_id) FROM user_agent_for_client_tbl b WHERE b.user_agent = a.user_agent);", userAgent);
					} else {
						int minId = selectInt(logger, "SELECT MIN(user_agent_id) FROM user_agent_for_client_tbl WHERE user_agent = ?", userAgent);
						update(logger, "DELETE FROM user_agent_for_client_tbl WHERE user_agent = ? AND user_agent_id > ?", userAgent, minId);
					}
				} else if (touchedLines < 1) {
					logger.error("Error writing UserAgent: " + userAgent);
				}
			}
		}
	}

	public ComUserAgent getUserAgent(String userAgent) {
		return selectObjectDefaultNull(logger, "SELECT * FROM user_agent_tbl WHERE user_agent = ?", new ComUserAgentRowMapper(), userAgent);
	}

	@DaoUpdateReturnValueCheck
	public void deleteUserAgent(int id) {
		update(logger, "delete from user_agent_tbl where user_agent_id=?", id);
	}

    private class ComUserAgentRowMapper implements RowMapper<ComUserAgent> {
		@Override
		public ComUserAgent mapRow(ResultSet resultSet, int row) throws SQLException {
			ComUserAgent userAgent = new ComUserAgent();
			userAgent.setId(resultSet.getBigDecimal("user_agent_id").intValue());
			userAgent.setUserAgent(resultSet.getString("user_agent"));
			userAgent.setReqCounter(resultSet.getBigDecimal("req_counter").intValue());
			userAgent.setCreationDate(resultSet.getTimestamp("creation_date"));
			userAgent.setChangeDate(resultSet.getTimestamp("change_date"));
			return userAgent;
		}
	}

}
