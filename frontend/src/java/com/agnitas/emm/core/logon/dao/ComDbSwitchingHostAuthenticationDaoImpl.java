/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.dao;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.supervisor.beans.Supervisor;

/**
 * This implementation of {@link ComHostAuthenticationDao} implements Facade pattern to
 * switch between Oracle- and MySQL-specific code.
 */
public class ComDbSwitchingHostAuthenticationDaoImpl extends BaseDaoImpl implements ComHostAuthenticationDao {

	/** Oracle-specific implementation of {@link ComHostAuthenticationDao}. */
	private ComHostAuthenticationDao oracleDao;

	/** MySQL-specific implementation of {@link ComHostAuthenticationDao}. */
	private ComHostAuthenticationDao mysqlDao;

	/**
	 * Returns database specific implementation of {@link ComHostAuthenticationDao}.
	 *
	 * @return database specific implementation of {@link ComHostAuthenticationDao}
	 */
	private ComHostAuthenticationDao getDbBasedImplementation() {
		if( isOracleDB())
			return oracleDao;
		else
			return mysqlDao;
	}
	
	@Override
	public String getSecurityCode(ComAdmin admin, String hostID) throws HostAuthenticationDaoException, NoSecurityCodeHostAuthenticationDaoException {
		return getDbBasedImplementation().getSecurityCode(admin, hostID);
	}
	
	@Override
	public String getSecurityCode(Supervisor supervisor, String hostID) throws HostAuthenticationDaoException, NoSecurityCodeHostAuthenticationDaoException {
		return getDbBasedImplementation().getSecurityCode(supervisor, hostID);
	}
	
	@Override
	public void writePendingSecurityCode(ComAdmin admin, String hostID, String securityCode) throws HostAuthenticationDaoException {
		getDbBasedImplementation().writePendingSecurityCode(admin, hostID, securityCode);
	}
	
	@Override
	public void writePendingSecurityCode(Supervisor supervisor, String hostID, String securityCode) throws HostAuthenticationDaoException {
		getDbBasedImplementation().writePendingSecurityCode(supervisor, hostID, securityCode);
	}
	
	@Override
	public boolean isHostAuthenticated(ComAdmin admin, String hostID) throws HostAuthenticationDaoException {
		return getDbBasedImplementation().isHostAuthenticated(admin, hostID);
	}
	
	@Override
	public boolean isHostAuthenticated(Supervisor supervisor, String hostID) throws HostAuthenticationDaoException {
		return getDbBasedImplementation().isHostAuthenticated(supervisor, hostID);
	}
	
	@Override
	public void writeHostAuthentication(ComAdmin admin, String hostID, int expiresInDays) throws HostAuthenticationDaoException {
		getDbBasedImplementation().writeHostAuthentication(admin, hostID, expiresInDays);
	}
	
	@Override
	public void writeHostAuthentication(Supervisor supervisor, String hostID, int expiresInDays) throws HostAuthenticationDaoException {
		getDbBasedImplementation().writeHostAuthentication(supervisor, hostID, expiresInDays);
	}
	
	@Override
	public void removePendingSecurityCode(ComAdmin admin, String hostID) throws HostAuthenticationDaoException {
		getDbBasedImplementation().removePendingSecurityCode(admin, hostID);
	}
	
	@Override
	public void removePendingSecurityCode(Supervisor supervisor, String hostID) throws HostAuthenticationDaoException {
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
	 * Set Oracle specific implementation of of {@link ComHostAuthenticationDao}.
	 * 
	 * @param dao Oracle specific implementation
	 */
	@Required
	public void setOracleImplementation( ComHostAuthenticationDao dao) {
		this.oracleDao = dao;
	}

	/**
	 * Set MySQL specific implementation of of {@link ComHostAuthenticationDao}.
	 * 
	 * @param dao MySQL specific implementation
	 */
	@Required
	public void setMysqlImplementation( ComHostAuthenticationDao dao) {
		this.mysqlDao = dao;
	}

}
