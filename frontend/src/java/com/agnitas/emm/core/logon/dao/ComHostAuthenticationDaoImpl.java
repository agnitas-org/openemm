/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.supervisor.beans.Supervisor;

/**
 * Implementation of {@link ComHostAuthenticationDao}.
 * This implementation contains common SQL queries, which can (hopefully) be used for all SQL databases.
 * 
 *  If a database-specific query is required, create subclass of this implementation and use {@link ComDbSwitchingHostAuthenticationDaoImpl} for
 *  to switch code.
 * 
 * @see ComDbSwitchingHostAuthenticationDaoImpl
 */

/*
 * DO NOT INHERIT FROM BaseDaoImpl!!!
 * 
 * BaseDaoImpl provides methods to switch code between MySQL and Oracle. DO NOT USE!
 * Write separate implementations for different databases and use switching code on top of these classes in 
 * an additional class by using Facade pattern!
 * 
 * Use this class (ComHostAuthenticationDaoImpl) as base class for common SQL statements and inherit from this class for Oracle, MySQL, ...
 * to implement database specific queries.
 */
public class ComHostAuthenticationDaoImpl extends BaseDaoImpl implements ComHostAuthenticationDao {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComHostAuthenticationDaoImpl.class);
	
	@Override
	public String getSecurityCode(ComAdmin admin, String hostID) throws HostAuthenticationDaoException, NoSecurityCodeHostAuthenticationDaoException {
		int adminID = admin.getAdminID();
		
		if (logger.isInfoEnabled()) {
			logger.info("Reading security code for admin " + adminID + " on host " + hostID);
		}

		List<String> list = select(logger, "SELECT security_code FROM hostauth_pending_tbl WHERE admin_id = ? AND host_id = ?", new HostAuthenticationSecurityCodeRowMapper(), adminID, hostID);

		if (list.size() == 0) {
			throw new NoSecurityCodeHostAuthenticationDaoException();
		} else if (list.size() > 1) {
			throw new HostAuthenticationDaoException("Found " + list.size() + " security codes for admin " + adminID + " on host " + hostID);
		}

		return list.get(0);
	}
	
	@Override
	public String getSecurityCode(Supervisor supervisor, String hostID) throws HostAuthenticationDaoException, NoSecurityCodeHostAuthenticationDaoException {
		int supervisorID = supervisor.getId();
		
		if (logger.isInfoEnabled()) {
			logger.info("Reading security code for supervisor " + supervisorID + " on host " + hostID);
		}

		List<String> list = select(logger, "SELECT security_code FROM hostauth_pending_sv_tbl WHERE supervisor_id = ? AND host_id = ?", new HostAuthenticationSecurityCodeRowMapper(), supervisorID, hostID);

		if (list.size() == 0) {
			throw new NoSecurityCodeHostAuthenticationDaoException();
		} else if (list.size() > 1) {
			throw new HostAuthenticationDaoException("Found " + list.size() + " security codes for supervisor " + supervisorID + " on host " + hostID);
		}

		return list.get(0);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void writePendingSecurityCode(ComAdmin admin, String hostID, String securityCode) throws HostAuthenticationDaoException {
		int adminID = admin.getAdminID();
		
		if (logger.isInfoEnabled()) {
			logger.info("Writing new pending security code for admin " + adminID + " on host " + hostID);
		}

		update(logger, "INSERT INTO hostauth_pending_tbl (admin_id, host_id, security_code) VALUES (?, ?, ?)", adminID, hostID, securityCode);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void writePendingSecurityCode(Supervisor supervisor, String hostID, String securityCode) throws HostAuthenticationDaoException {
		int supervisorID = supervisor.getId();
		
		if (logger.isInfoEnabled()) {
			logger.info("Writing new pending security code for supervisor " + supervisorID + " on host " + hostID);
		}

		update(logger, "INSERT INTO hostauth_pending_sv_tbl (supervisor_id, host_id, security_code) VALUES (?, ?, ?)", supervisorID, hostID, securityCode);
	}

	@Override
	public boolean isHostAuthenticated(ComAdmin admin, String hostID) throws HostAuthenticationDaoException {
		int adminID = admin.getAdminID();
		
		if (logger.isInfoEnabled()) {
			logger.info("Checking host authentication for admin " + adminID + " on host " + hostID);
		}

		int count = selectInt(logger, "SELECT count(*) FROM authenticated_hosts_tbl WHERE admin_id = ? AND host_id = ? AND expire_date >= CURRENT_TIMESTAMP", adminID, hostID);

		if (logger.isInfoEnabled()) {
			logger.info("Found " + count + " host authentication records for admin " + adminID + " on host " + hostID);
		}

		return count > 0;
	}

	@Override
	public boolean isHostAuthenticated(Supervisor supervisor, String hostID) throws HostAuthenticationDaoException {
		int supervisorID = supervisor.getId();
		
		if (logger.isInfoEnabled()) {
			logger.info("Checking host authentication for supervisor " + supervisorID + " on host " + hostID);
		}

		int count = selectInt(logger, "SELECT count(*) FROM authenticated_hosts_sv_tbl WHERE supervisor_id = ? AND host_id = ? AND expire_date >= CURRENT_TIMESTAMP", supervisorID, hostID);

		if (logger.isInfoEnabled()) {
			logger.info("Found " + count + " host authentication records for supervisor " + supervisorID + " on host " + hostID);
		}

		return count > 0;
	}

	@Override
	public void writeHostAuthentication(ComAdmin admin, String hostID, int expiresInDays) throws HostAuthenticationDaoException {
		int adminID = admin.getAdminID();
		
		if (logger.isInfoEnabled()) {
			logger.info("Writing host authentication for admin " + adminID + " on host " + hostID);
		}
		
		try {
			if (!renewHostAuthenticationData(admin, hostID, expiresInDays)) {
				if (logger.isInfoEnabled()) {
					logger.info("Writing new host authentication data for admin " + adminID + " on host " + hostID);
				}

				writeNewHostAuthenticationData(admin, hostID, expiresInDays);
			} else {
				if (logger.isInfoEnabled()) {
					logger.info("Renewed host authentication data for admin " + adminID + " on host " + hostID);
				}
			}
		} catch (Exception e) {
			String message = "Error writing host authentication data for admin " + adminID + " on host + " + hostID;

			logger.error(message);

			throw new HostAuthenticationDaoException(message, e);
		}
	}

	@Override
	public void writeHostAuthentication(Supervisor supervisor, String hostID, int expiresInDays) throws HostAuthenticationDaoException {
		int supervisorID = supervisor.getId();
		
		if (logger.isInfoEnabled()) {
			logger.info("Writing host authentication for supervisor " + supervisorID + " on host " + hostID);
		}
		
		try {
			if (!renewHostAuthenticationData(supervisor, hostID, expiresInDays)) {
				if (logger.isInfoEnabled()) {
					logger.info("Writing new host authentication data for supervisor " + supervisorID + " on host " + hostID);
				}

				writeNewHostAuthenticationData(supervisor, hostID, expiresInDays);
			} else {
				if (logger.isInfoEnabled()) {
					logger.info("Renewed host authentication data for supervisor " + supervisorID + " on host " + hostID);
				}
			}
		} catch (Exception e) {
			String message = "Error writing host authentication data for supervisor " + supervisorID + " on host + " + hostID;

			logger.error(message);

			throw new HostAuthenticationDaoException(message, e);
		}
	}

	/**
	 * Try to set new expiration date for existing host authentication data.
	 * 
	 * @param admin admin
	 * @param hostID host ID
	 * @param expiresInDays duration of validity in days of authentication data
	 * 
	 * @return {@code true} if renewal was successful, otherwise {@code false}
	 */
	@DaoUpdateReturnValueCheck
	private boolean renewHostAuthenticationData(ComAdmin admin, String hostID, int expiresInDays) {
		int adminID = admin.getAdminID();
		Date expireDate = computeExpireDate(expiresInDays);

		// Update authentication data
		int result = update(logger, "UPDATE authenticated_hosts_tbl SET expire_date = ?, change_date = CURRENT_TIMESTAMP WHERE admin_id = ? AND host_id = ?", expireDate, adminID, hostID);

		return result > 0;
	}

	/**
	 * Try to set new expiration date for existing host authentication data.
	 * 
	 * @param supervisor supervisor
	 * @param hostID host ID
	 * @param expiresInDays duration of validity in days of authentication data
	 * 
	 * @return {@code true} if renewal was successful, otherwise {@code false}
	 */
	@DaoUpdateReturnValueCheck
	private boolean renewHostAuthenticationData(Supervisor supervisor, String hostID, int expiresInDays) {
		int supervisorID = supervisor.getId();
		Date expireDate = computeExpireDate(expiresInDays);

		// Update authentication data
		int result = update(logger, "UPDATE authenticated_hosts_sv_tbl SET expire_date = ?, change_date = CURRENT_TIMESTAMP WHERE supervisor_id = ? AND host_id = ?", expireDate, supervisorID, hostID);

		return result > 0;
	}

	/**
	 * Write new host authentication data.
	 * 
	 * @param admin admin
	 * @param hostID host ID
	 * @param expiresInDays duration of validity in days of authentication data
	 */
	@DaoUpdateReturnValueCheck
	private void writeNewHostAuthenticationData(ComAdmin admin, String hostID, int expiresInDays) {
		int adminID = admin.getAdminID();
		Date expireDate = computeExpireDate(expiresInDays);

		// Write authentication data
		update(logger, "INSERT INTO authenticated_hosts_tbl (admin_id, host_id, expire_date, change_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", adminID, hostID, expireDate);
	}

	/**
	 * Write new host authentication data.
	 * 
	 * @param supervisor supervisor
	 * @param hostID host ID
	 * @param expiresInDays duration of validity in days of authentication data
	 */
	@DaoUpdateReturnValueCheck
	private void writeNewHostAuthenticationData(Supervisor supervisor, String hostID, int expiresInDays) {
		int supervisorID = supervisor.getId();
		Date expireDate = computeExpireDate(expiresInDays);

		// Write authentication data
		update(logger, "INSERT INTO authenticated_hosts_sv_tbl (supervisor_id, host_id, expire_date, change_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", supervisorID, hostID, expireDate);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void removePendingSecurityCode(ComAdmin admin, String hostID) throws HostAuthenticationDaoException {
		int adminID = admin.getAdminID();
		update(logger, "DELETE FROM hostauth_pending_tbl WHERE admin_id = ? AND host_id = ?", adminID, hostID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void removePendingSecurityCode(Supervisor supervisor, String hostID) throws HostAuthenticationDaoException {
		int supervisorID = supervisor.getId();
		update(logger, "DELETE FROM hostauth_pending_sv_tbl WHERE supervisor_id = ? AND host_id = ?", supervisorID, hostID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void removeExpiredHostAuthentications() {
		if (logger.isInfoEnabled()) {
			logger.info("Removing expired host authentications");
		}
		
		// Remove pending host authentications for admins
		int count = update(logger, "DELETE FROM authenticated_hosts_tbl WHERE expire_date < CURRENT_TIMESTAMP");
		// Remove pending host authentications for supervisors
		count += update(logger, "DELETE FROM authenticated_hosts_sv_tbl WHERE expire_date < CURRENT_TIMESTAMP");

		if (logger.isInfoEnabled()) {
			logger.info("Removed " + count + " expired host authentications.");
		}
	}

	@Override
	public void removeAuthentictedHost(final String hostId) {
		final String sql= "DELETE FROM authenticated_hosts_tbl WHERE host_id=?";
		
		update(logger, sql, hostId);
	}


	@Override
	public final void removeExpiredPendingsAuthentications(final int maxPendingHostAuthenticationsAgeMinutes) {
		if(logger.isInfoEnabled()) {
			logger.info(String.format("Removing all pending host authentications older than %d minutes", maxPendingHostAuthenticationsAgeMinutes));
		}
		
		final Calendar threshold = new GregorianCalendar();
		threshold.add(Calendar.MINUTE, -maxPendingHostAuthenticationsAgeMinutes);
		
		int count = update(logger, "DELETE FROM hostauth_pending_tbl WHERE creation_date < ?", threshold);
		count += update(logger, "DELETE FROM hostauth_pending_sv_tbl WHERE creation_date < ?", threshold);
		
		if (logger.isInfoEnabled()) {
			logger.info("Removed " + count + " expired pending host authentications.");
		}
	}
	
	/**
	 * Computes expire date.
	 * 
	 * @param expiresInDays
	 *            period of validity in days from now
	 * 
	 * @return expire date
	 */
	private static Date computeExpireDate(int expiresInDays) {
		GregorianCalendar now = new GregorianCalendar();
		now.add(Calendar.DAY_OF_MONTH, expiresInDays);

		return now.getTime();
	}
	
	/**
	 * Row mapper for mapping security codes to {@link java.lang.String}.
	 */
	protected class HostAuthenticationSecurityCodeRowMapper implements RowMapper<String> {
		@Override
		public String mapRow(ResultSet resultSet, int row) throws SQLException {
			return resultSet.getString("security_code");
		}
	}

}
