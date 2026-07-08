/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.loginmanager.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.loginmanager.bean.LoginData;
import com.agnitas.emm.core.loginmanager.dao.LoginTrackDao;
import com.agnitas.emm.core.loginmanager.enums.LoginStatus;
import org.springframework.jdbc.core.RowMapper;

/**
 * Abstract DAO implementation for login tracking.
 */
public abstract class AbstractLoginTrackDaoImpl extends BaseDaoImpl implements LoginTrackDao {
	
	private static final RowMapper<LoginData> LOGIN_DATA_ROW_MAPPER = (rs, i) -> new LoginData(
		rs.getInt("login_track_id"),
		new Date(rs.getTimestamp("creation_date").getTime()), // Required. Otherwise java.util.Date.before() does not work correctly
		rs.getString("ip_address"),
		LoginStatus.getLoginStatusFromStatusCode(rs.getInt( "login_status")),
		rs.getString("username")
	);

	@Override
	public final List<LoginData> listLoginData(final Date sinceOrNull) {
		if(sinceOrNull == null) {
			final String sql = String.format("SELECT * FROM %s ORDER BY creation_date DESC", getTrackingTableName());
			
			return select(sql, LOGIN_DATA_ROW_MAPPER);
		} else {
			final String sql = String.format("SELECT * FROM %s WHERE creation_date > ? ORDER BY creation_date DESC", getTrackingTableName());
					
			return select(sql, LOGIN_DATA_ROW_MAPPER, sinceOrNull);
		}
	}

	@Override
	public final List<LoginData> listLoginDataByIpAddress(final String ipAddress, final Date sinceOrNull) {
		if(sinceOrNull == null) {
			final String sql = String.format("SELECT * FROM %s WHERE ip_address = ? ORDER BY creation_date DESC", getTrackingTableName());
			
			return select(sql, LOGIN_DATA_ROW_MAPPER, ipAddress);
		} else {
			final String sql = String.format("SELECT * FROM %s WHERE creation_date > ? AND ip_address = ? ORDER BY creation_date DESC", getTrackingTableName());
					
			return select(sql, LOGIN_DATA_ROW_MAPPER, sinceOrNull, ipAddress);
		}
	}
	
	@Override
	public final List<LoginData> listLoginDataByUsername(final String username, final Date sinceOrNull) {
		if(sinceOrNull == null) {
			final String sql = String.format("SELECT * FROM %s WHERE username = ? ORDER BY creation_date DESC", getTrackingTableName());
			
			return select(sql, LOGIN_DATA_ROW_MAPPER, username);
		} else {
			final String sql = String.format("SELECT * FROM %s WHERE creation_date > ? AND username = ? ORDER BY creation_date DESC", getTrackingTableName());
				
			return select(sql, LOGIN_DATA_ROW_MAPPER, sinceOrNull, username);
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
		
		update(sql, ipAddress, status.getStatusCode(), username);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public int deleteOldRecordsHours(int holdBackHours, int maxRecords) {
		if (holdBackHours < 0) {
			throw new IllegalArgumentException("holdBackHours must be >= 0");
		}
		if (maxRecords < 0) {
			throw new IllegalArgumentException("maxRecords must be >= 0");
		}
		return update(getDeleteOldRecordsSql(), holdBackHours, maxRecords);
	}

	private String getDeleteOldRecordsSql() {
		if (isOracleDB()) {
			return "DELETE FROM %s WHERE creation_date < sysdate - ? / 24.0 AND ROWNUM <= ?"
				.formatted(getTrackingTableName());
		} else if (isPostgreSQL()) {
			return """
				DELETE FROM %s
				WHERE ctid IN (
				    SELECT ctid
				    FROM %s
				    WHERE creation_date < CURRENT_TIMESTAMP - (? * INTERVAL '1 hour')
				    LIMIT ?
				)
				""".formatted(getTrackingTableName(), getTrackingTableName());
		} else {
			return "DELETE FROM %s WHERE DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? HOUR) > creation_date LIMIT ?"
				.formatted(getTrackingTableName());
		}
	}

	@Override
	public Optional<LoginData> findLoginDataByTrackingID(final long trackingId) {
		final List<LoginData> list = select(
			"SELECT * FROM %s WHERE login_track_id = ?".formatted(getTrackingTableName()),
			LOGIN_DATA_ROW_MAPPER, trackingId);
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	/**
	 * Returns the name of the tracking table used by subclass.
	 * 
	 * @return name of tracking table
	 */
	public abstract String getTrackingTableName();
}
