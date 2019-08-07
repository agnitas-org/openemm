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
import java.util.List;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.loginmanager.entity.BlockedAddressData;
import org.agnitas.beans.FailedLoginData;
import org.agnitas.beans.impl.FailedLoginDataImpl;
import org.agnitas.dao.LoginTrackDao;
import org.agnitas.emm.core.logintracking.LoginStatus;
import org.agnitas.emm.core.logintracking.bean.LoginData;
import org.agnitas.emm.core.logintracking.bean.LoginDataImpl;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

/**
 * DAO implementation for login tracking using a MySQL database.
 */
public class LoginTrackDaoImpl extends PaginatedBaseDaoImpl implements LoginTrackDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(LoginTrackDaoImpl.class);
	
	/**
	 * Status for a successful login
	 */
	public static final int LOGIN_TRACK_STATUS_SUCCESS = 10;
	
	/**
	 * Status for a failed login
	 */
	public static final int LOGIN_TRACK_STATUS_FAILED = 20;
	
	/**
	 * Status for a successful login during lock period
	 */
	public static final int LOGIN_TRACK_STATUS_DURING_BLOCK = 40;
	
	/**
	 * RowMapper for DB access
	 */
	protected final static RowMapper<FailedLoginData> FAILED_LOGIN_DATA_ROW_MAPPER = new FailedLoginDataRowMapper();
	
	/** Row mapper for login track data. */
	protected final static RowMapper<LoginData> LOGIN_DATA_ROW_MAPPER = new LoginDataRowMapper();
	
	protected final static RowMapper<BlockedAddressData> BLOCKED_ADDRESS_DATA_ROW_MAPPER = new BlockedAddressDataRowMapper();
	
	/**
	 * Implementation of the RowMapper
	 */
	static class FailedLoginDataRowMapper implements RowMapper<FailedLoginData> {
		@Override
		public FailedLoginData mapRow(ResultSet rs, int row) throws SQLException {
			FailedLoginDataImpl result = new FailedLoginDataImpl();
			
			result.setNumFailedLogins(rs.getInt(1));
			result.setLastFailedLoginTimeDifference(rs.getInt(2));
			
			return result;
		}
	
	}
	
	/**
	 * Implementation of {@link RowMapper} for all kinds of login track data.
	 */
	static class LoginDataRowMapper implements RowMapper<LoginData> {

		@Override
		public LoginData mapRow(ResultSet rs, int row) throws SQLException {
			int trackId = rs.getInt( "login_track_id");
			Date loginTime = new Date( rs.getTimestamp( "creation_date").getTime());	// Required. Otherwise java.util.Date.before() does not work correctly
			String loginIP = rs.getString( "ip_address");
			LoginStatus loginStatus = LoginStatus.getLoginStatusFromStatusCode( rs.getInt( "login_status"));
			String username = rs.getString( "username");
			 
			
			return new LoginDataImpl( trackId, loginTime, loginIP, loginStatus, username);
		}
	}
	
	static class BlockedAddressDataRowMapper implements RowMapper<BlockedAddressData> {
		
		@Override
		public BlockedAddressData mapRow(ResultSet resultSet, int i) throws SQLException {
			BlockedAddressData blockedAddressData = new BlockedAddressData();
			blockedAddressData.setUsername(resultSet.getString("username"));
			blockedAddressData.setIpAddress(resultSet.getString("ip_address"));
			blockedAddressData.setId(resultSet.getInt("login_track_id"));
			return blockedAddressData;
		}
	}

	@Override
	public FailedLoginData getFailedLoginData(String ipAddress) {
		try {
			String sql = "SELECT count(ip_address), ifnull(timestampdiff(second, max(ifnull(creation_date, 0)), now()),0) " +
					"FROM login_track_tbl " +
					"WHERE ip_address = ? " +
					"AND login_status = " + LoginTrackDaoImpl.LOGIN_TRACK_STATUS_FAILED + " " +
					"AND creation_date > (" +
					"     SELECT ifnull(max(creation_date), 0) " +
					"     FROM login_track_tbl " +
					"     WHERE ip_address = ? " +
					"     AND login_status = " + LoginTrackDaoImpl.LOGIN_TRACK_STATUS_SUCCESS + ")";
			
			List<FailedLoginData> list = select(logger, sql, FAILED_LOGIN_DATA_ROW_MAPPER, ipAddress, ipAddress);
			
			if (list.size() == 1) {
				return list.get(0);
			}
		} catch (Exception e) {
			logger.warn("Could not find failed login data");
		}
		
		return new FailedLoginDataImpl();  // No failed logins found
	}

	@Override
	public void trackFailedLogin(String ipAddress, String username) {
		trackLoginStatus(ipAddress, username, LoginTrackDaoImpl.LOGIN_TRACK_STATUS_FAILED);
	}

	@Override
	public void trackSuccessfulLogin(String ipAddress, String username) {
		trackLoginStatus(ipAddress, username, LoginTrackDaoImpl.LOGIN_TRACK_STATUS_SUCCESS);
	}
	
	@Override
	public void trackLoginDuringBlock(String ipAddress, String username) {
		trackLoginStatus(ipAddress, username, LoginTrackDaoImpl.LOGIN_TRACK_STATUS_DURING_BLOCK);
	}
	
	/**
	 * Generic method for recording logins.
	 * 
	 * @param ipAddress IP address of host
	 * @param username use username in login
	 * @param status login status
	 */
	@DaoUpdateReturnValueCheck
	protected void trackLoginStatus(String ipAddress, String username, int status) {
		String sql = "INSERT INTO login_track_tbl (ip_address, login_status, username) VALUES (?, ?, ?)";
		
		update(logger, sql, ipAddress, status, username);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public int deleteOldRecords(int holdBackDays, int maxRecords) throws Exception {
		String sql = "DELETE FROM login_track_tbl WHERE TIMESTAMPDIFF(day, creation_date, now()) > ? LIMIT ?";
		
		return update(logger, sql, holdBackDays, maxRecords);
	}

	@Override
	public LoginData getLastSuccessfulLogin(String username, boolean skipLastSuccess) {
		LoginData login = getLastSuccessfulLoginBefore( username, null);
		
		if( login == null)
			return null;
		
		// Skip this login? Query again, this time with time...
		if( skipLastSuccess)
			login = getLastSuccessfulLoginBefore( username, login.getLoginTime());
		
		return login;
	}
	
	/**
	 * Returns the last successful login before given date.
	 * {@code before} can be {@code null} to disable check for date. 
	 * 
	 * @param username user name 
	 * @param before get last successful login before this date or {@code null}
	 * 
	 * @return last successful login
	 */
	private LoginData getLastSuccessfulLoginBefore( String username, Date before) {
		// This query works with Oracle and MySQL
		String query = "SELECT login_track_id, username, creation_date, ip_address, login_status " +
				"FROM login_track_tbl WHERE login_track_id = " +
				"(SELECT MAX(login_track_id) FROM login_track_tbl WHERE username = ? AND login_status = 10 AND (creation_date < ? OR ? IS NULL))";
		
		List<LoginData> list = select(logger, query, LOGIN_DATA_ROW_MAPPER, username, before, before);
		if( list.size() == 0)
			return null;
		else
			return list.get( 0);
	}

	@Override
	public int countFailedLogins(String username, Date since) {
		String query = "SELECT COUNT(login_track_id) AS fails FROM login_track_tbl WHERE username = ? " +
				"AND (creation_date >= ? or ? IS NULL) " +
				"AND login_status = 20";
		
		return selectInt(logger, query, username, since, since);
	}

	@Override
	public List<LoginData> getLoginAttemptsSince(String username, Date since) {
		String query = "SELECT login_track_id, username, creation_date, ip_address, login_status " +
				"FROM login_track_tbl WHERE username = ? AND creation_date >= ? AND login_status IN (?, ?, ?) " +
				"ORDER BY creation_date DESC";
		
		return select(logger, query, LOGIN_DATA_ROW_MAPPER,
				username, 
				since, 
				LoginStatus.SUCCESS.getStatusCode(),
				LoginStatus.FAIL.getStatusCode(),
				LoginStatus.SUCCESS_BUT_BLOCKED.getStatusCode());
	}
	
}
