/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.dao;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.beans.Admin;
import com.agnitas.emm.core.supervisor.beans.Supervisor;

/**
 * This implementation of {@link HostAuthenticationDao} implements Facade pattern to
 * switch between Oracle- and MySQL-specific code.
 */
public class DbSwitchingHostAuthenticationDaoImpl extends BaseDaoImpl implements HostAuthenticationDao {

	/** Oracle-specific implementation of {@link HostAuthenticationDao}. */
	private HostAuthenticationDao oracleDao;

	/** MySQL-specific implementation of {@link HostAuthenticationDao}. */
	private HostAuthenticationDao mysqlDao;

	/**
	 * Returns database specific implementation of {@link HostAuthenticationDao}.
	 *
	 * @return database specific implementation of {@link HostAuthenticationDao}
	 */
	private HostAuthenticationDao getDbBasedImplementation() {
		if( isOracleDB()) {
			return oracleDao;
		} else {
			return mysqlDao;
		}
	}
	
	@Override
	public String getSecurityCode(Admin admin, String hostID) throws HostAuthenticationDaoException {
		return getDbBasedImplementation().getSecurityCode(admin, hostID);
	}
	
	@Override
	public String getSecurityCode(Supervisor supervisor, String hostID) throws HostAuthenticationDaoException {
		return getDbBasedImplementation().getSecurityCode(supervisor, hostID);
	}
	
	@Override
	public void writePendingSecurityCode(Admin admin, String hostID, String securityCode) {
		getDbBasedImplementation().writePendingSecurityCode(admin, hostID, securityCode);
	}
	
	@Override
	public void writePendingSecurityCode(Supervisor supervisor, String hostID, String securityCode) {
		getDbBasedImplementation().writePendingSecurityCode(supervisor, hostID, securityCode);
	}
	
	@Override
	public boolean isHostAuthenticated(Admin admin, String hostID) {
		return getDbBasedImplementation().isHostAuthenticated(admin, hostID);
	}
	
	@Override
	public boolean isHostAuthenticated(Supervisor supervisor, String hostID) {
		return getDbBasedImplementation().isHostAuthenticated(supervisor, hostID);
	}
	
	@Override
	public void writeHostAuthentication(Admin admin, String hostID, int expiresInDays) throws HostAuthenticationDaoException {
		getDbBasedImplementation().writeHostAuthentication(admin, hostID, expiresInDays);
	}
	
	@Override
	public void writeHostAuthentication(Supervisor supervisor, String hostID, int expiresInDays) throws HostAuthenticationDaoException {
		getDbBasedImplementation().writeHostAuthentication(supervisor, hostID, expiresInDays);
	}
	
	@Override
	public void removePendingSecurityCode(Admin admin, String hostID) {
		getDbBasedImplementation().removePendingSecurityCode(admin, hostID);
	}
	
	@Override
	public void removePendingSecurityCode(Supervisor supervisor, String hostID) {
		getDbBasedImplementation().removePendingSecurityCode(supervisor, hostID);
	}
	
	@Override
	public void removeExpiredHostAuthentications() {
		getDbBasedImplementation().removeExpiredHostAuthentications();
	}
	
	@Override
	public final void removeExpiredPendingsAuthentications(final int maxPendingHostAuthenticationsAgeMinutes) {
		getDbBasedImplementation().removeExpiredPendingsAuthentications(maxPendingHostAuthenticationsAgeMinutes);
	}

	@Override
	public void removeAuthentictedHost(String hostId) {
		getDbBasedImplementation().removeAuthentictedHost(hostId);
	}
	
	// ---------------------------------------------------------------------------------------------------- dependency injection
	/**
	 * Set Oracle specific implementation of of {@link HostAuthenticationDao}.
	 * 
	 * @param dao Oracle specific implementation
	 */
	public void setOracleImplementation( HostAuthenticationDao dao) {
		this.oracleDao = dao;
	}

	/**
	 * Set MySQL specific implementation of of {@link HostAuthenticationDao}.
	 * 
	 * @param dao MySQL specific implementation
	 */
	public void setMysqlImplementation( HostAuthenticationDao dao) {
		this.mysqlDao = dao;
	}

}
