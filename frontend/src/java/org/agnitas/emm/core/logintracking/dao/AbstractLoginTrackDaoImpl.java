/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.logintracking.LoginStatus;
import org.agnitas.emm.core.logintracking.bean.LoginData;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

/**
 * Abstract DAO implementation for login tracking.
 */
public abstract class AbstractLoginTrackDaoImpl extends BaseDaoImpl implements LoginTrackDao {
	
	/** Row mapper for login track data. */
	private final static RowMapper<LoginData> LOGIN_DATA_ROW_MAPPER = new LoginDataRowMapper();
	
	/**
	 * Implementation of {@link RowMapper} for all kinds of login track data.
	 */
	static class LoginDataRowMapper implements RowMapper<LoginData> {

		@Override
		public LoginData mapRow(ResultSet rs, int row) throws SQLException {
			final int trackId = rs.getInt("login_track_id");
			final Date loginTime = new Date(rs.getTimestamp("creation_date").getTime());	// Required. Otherwise java.util.Date.before() does not work correctly
			final String loginIP = rs.getString("ip_address");
			final LoginStatus loginStatus = LoginStatus.getLoginStatusFromStatusCode(rs.getInt( "login_status"));
			final String username = rs.getString("username");
			
			return new LoginData(trackId, loginTime, loginIP, loginStatus, username);
		}
	}
	
	@Override
	public final List<LoginData> listLoginData(final Date sinceOrNull) {
		if(sinceOrNull == null) {
			final String sql = String.format("SELECT * FROM %s ORDER BY creation_date DESC", getTrackingTableName());
			
			return select(getLogger(), sql, new LoginDataRowMapper());
		} else {		
			final String sql = String.format("SELECT * FROM %s WHERE creation_date > ? ORDER BY creation_date DESC", getTrackingTableName());
					
			return select(getLogger(), sql, new LoginDataRowMapper(), sinceOrNull);
		}
	}

	@Override
	public final List<LoginData> listLoginDataByIpAddress(final String ipAddress, final Date sinceOrNull) {
		if(sinceOrNull == null) {
			final String sql = String.format("SELECT * FROM %s WHERE ip_address = ? ORDER BY creation_date DESC", getTrackingTableName());
			
			return select(getLogger(), sql, new LoginDataRowMapper(), ipAddress);
		} else {		
			final String sql = String.format("SELECT * FROM %s WHERE creation_date > ? AND ip_address = ? ORDER BY creation_date DESC", getTrackingTableName());
					
			return select(getLogger(), sql, new LoginDataRowMapper(), sinceOrNull, ipAddress);
		}
	}
	
	@Override
	public final List<LoginData> listLoginDataByUsername(final String username, final Date sinceOrNull) {
		if(sinceOrNull == null) {
			final String sql = String.format("SELECT * FROM %s WHERE username = ? ORDER BY creation_date DESC", getTrackingTableName());
			
			return select(getLogger(), sql, new LoginDataRowMapper(), username);		
		} else {		
			final String sql = String.format("SELECT * FROM %s WHERE creation_date > ? AND username = ? ORDER BY creation_date DESC", getTrackingTableName());
				
			return select(getLogger(), sql, new LoginDataRowMapper(), sinceOrNull, username);		
		}
	}
	
	/**
	 * Generic method for recording logins.
	 * 
	 * @param ipAddress IP address of host
	 * @param username use username in login
	 * @param status login status
	 */
	@Override
	public void trackLoginStatus(String ipAddress, String username, final LoginStatus status) {
		final String sql = isOracleDB()
				? String.format("INSERT INTO %s (login_track_id, ip_address, login_status, username) VALUES (%s_seq.NEXTVAL, ?, ?, ?)", getTrackingTableName(), getTrackingTableName())
				: String.format("INSERT INTO %s (ip_address, login_status, username) VALUES (?, ?, ?)", getTrackingTableName());
		
		update(getLogger(), sql, ipAddress, status.getStatusCode(), username);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public int deleteOldRecords(int holdBackDays, int maxRecords) {
		if(holdBackDays < 0)
			throw new IllegalArgumentException("holdBackDays must be >= 0");
		if(maxRecords < 0)
			throw new IllegalArgumentException("maxRecords must be >= 0");
		
		final String sql = isOracleDB()
				? String.format("DELETE FROM %s WHERE (sysdate - creation_date) > ? AND ROWNUM <= ?", getTrackingTableName())
				: String.format("DELETE FROM %s WHERE DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? DAY) > creation_date LIMIT ?", getTrackingTableName());

		return update(getLogger(), sql, Math.max(holdBackDays, 0), Math.max(maxRecords, 0));
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public int deleteOldRecordsHours(int holdBackHours, int maxRecords) {
		if(holdBackHours < 0)
			throw new IllegalArgumentException("holdBackHours must be >= 0");
		if(maxRecords < 0)
			throw new IllegalArgumentException("maxRecords must be >= 0");
		
		final String sql = isOracleDB()
				? String.format("DELETE FROM %s WHERE creation_date < sysdate - ? / 24.0 AND ROWNUM <= ?", getTrackingTableName())
				: String.format("DELETE FROM %s WHERE DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? HOUR) > creation_date LIMIT ?", getTrackingTableName());

		return update(getLogger(), sql, Math.max(holdBackHours, 0), Math.max(maxRecords, 0));
	}
	
	@Override
	public Optional<LoginData> findLoginDataByTrackingID(final int trackingId) {
		final String sql = String.format("SELECT * FROM %s WHERE login_track_id=?", getTrackingTableName());
		
		final List<LoginData> list = select(getLogger(), sql, LOGIN_DATA_ROW_MAPPER, trackingId);
		
		if(list.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(list.get(0));
		}
	}

	/**
	 * Returns the name of the tracking table used by sub-class.
	 * 
	 * @return name of tracking table
	 */
	public abstract String getTrackingTableName();
	
	/**
	 * Returns the logger for sub-class.
	 * 
	 * @return logger
	 */
	public abstract Logger getLogger();
	
}
