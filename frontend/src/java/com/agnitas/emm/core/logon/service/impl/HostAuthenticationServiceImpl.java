/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.logon.dao.HostAuthenticationDao;
import com.agnitas.emm.core.logon.dao.HostAuthenticationDaoException;
import com.agnitas.emm.core.logon.dao.NoSecurityCodeHostAuthenticationDaoException;
import com.agnitas.emm.core.logon.service.HostAuthenticationSecurityCodeGenerator;
import com.agnitas.emm.core.logon.service.HostAuthenticationService;
import com.agnitas.emm.core.logon.service.HostAuthenticationServiceException;
import com.agnitas.emm.core.logon.web.CannotSendSecurityCodeException;
import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.service.SupervisorUtil;
import com.agnitas.emm.core.systemmessages.service.SystemMailMessageService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HostAuthenticationServiceImpl implements HostAuthenticationService {

	/** Validity period of authenticated hosts in days. */
	public static final int HOST_AUTHENTICATION_VALIDITY_PERIOD_DAYS = 90;

	private static final Logger logger = LogManager.getLogger(HostAuthenticationServiceImpl.class);

	private ConfigService configService;
	private HostAuthenticationSecurityCodeGenerator securityCodeGenerator;
	private HostAuthenticationDao hostAuthenticationDao;
	private SystemMailMessageService systemMailMessageService;

	@Override
	public boolean isHostAuthenticated(Admin admin, String hostId) {
		Supervisor supervisor = SupervisorUtil.extractSupervisor(admin);

		if (supervisor == null) {
			return this.hostAuthenticationDao.isHostAuthenticated(admin, hostId);
		} else {
			return this.hostAuthenticationDao.isHostAuthenticated(supervisor, hostId);
		}
	}

	@Override
	public boolean isHostAuthenticationEnabled(int companyID) {
		// Host authentication is enabled by default and will be disabled by special configuration
		boolean enabled = this.configService.getBooleanValue(ConfigValue.HostAuthentication, companyID);

		if (enabled) {
			logger.info("Host authentication is ENABLED for company {}", companyID);
		} else {
			logger.info("Host authentication is DISABLED for company {}", companyID);
		}

		return enabled;
	}

	@Override
	public void sendSecurityCode(Admin admin, String hostID) throws HostAuthenticationServiceException {
		Supervisor supervisor = SupervisorUtil.extractSupervisor(admin);
		try {
			String securityCode = null;

			try {
				if (supervisor == null) {
					securityCode = this.hostAuthenticationDao.getSecurityCode(admin, hostID);
					logger.info("Found security code for admin {} on host {}", admin.getAdminID(), hostID);
				} else {
					securityCode = this.hostAuthenticationDao.getSecurityCode(supervisor, hostID);
					logger.info("Found security code for supervisor {} on host {}", admin.getAdminID(), hostID);
				}
			} catch (NoSecurityCodeHostAuthenticationDaoException e) {
				securityCode = this.securityCodeGenerator.createSecurityCode();

				if (supervisor == null) {
					logger.info("Found no security code for admin {} on host {}. Creating new one.", admin.getAdminID(), hostID);
					this.hostAuthenticationDao.writePendingSecurityCode(admin, hostID, securityCode);
				} else {
					logger.info("Found no security code for supervisor {} on host {}. Creating new one.", admin.getAdminID(), hostID);
					this.hostAuthenticationDao.writePendingSecurityCode(supervisor, hostID, securityCode);
				}
			}

			systemMailMessageService.sendSecurityCodeMail(admin, securityCode);
		} catch (CannotSendSecurityCodeException e) {
			String msg = supervisor == null
					? "Error sending security code (admin %d, host %s)".formatted(admin.getAdminID(), hostID)
					: "Error sending security code (supervisor %d, host %s)".formatted(supervisor.getId(), hostID);

			logger.error(msg, e);
			throw e;
		} catch (Exception e) {
			String msg = supervisor == null
					? "Error sending security code (admin %d, host %s)".formatted(admin.getAdminID(), hostID)
					: "Error sending security code (supervisor %d, host %s)".formatted(supervisor.getId(), hostID);

			logger.error(msg, e);
			throw new CannotSendSecurityCodeException(msg, e);
		}
	}

	@Override
	public void writeHostAuthentication(Admin admin, String hostID) throws HostAuthenticationServiceException {
		Supervisor supervisor = SupervisorUtil.extractSupervisor(admin);

		try {
			if (supervisor == null) {
				this.hostAuthenticationDao.writeHostAuthentication(admin, hostID, HOST_AUTHENTICATION_VALIDITY_PERIOD_DAYS);
				this.hostAuthenticationDao.removePendingSecurityCode(admin, hostID);
			} else {
				this.hostAuthenticationDao.writeHostAuthentication(supervisor, hostID, HOST_AUTHENTICATION_VALIDITY_PERIOD_DAYS);
				this.hostAuthenticationDao.removePendingSecurityCode(supervisor, hostID);
			}
		} catch (HostAuthenticationDaoException e) {
			String msg = supervisor == null
					? "Error writing host authentication data for admin %d on host %s".formatted(admin.getAdminID(), hostID)
					: "Error writing host authentication data for supervisor %d on host %s".formatted(supervisor.getId(), hostID);

			logger.error(msg, e);
			throw new HostAuthenticationServiceException(msg, e);
		}
	}

	@Override
	public String getPendingSecurityCode(Admin admin, String hostID) throws HostAuthenticationServiceException {
		Supervisor supervisor = SupervisorUtil.extractSupervisor(admin);

		try {
			if (supervisor == null) {
				return this.hostAuthenticationDao.getSecurityCode(admin, hostID);
			} else {
				return this.hostAuthenticationDao.getSecurityCode(supervisor, hostID);
			}
		} catch (NoSecurityCodeHostAuthenticationDaoException e) {
			String msg = supervisor == null
					? "No pending security code found for admin %d on host %s".formatted(admin.getAdminID(), hostID)
					: "No pending security code found for supervisor %d on host %s".formatted(supervisor.getId(), hostID);

			logger.warn(msg, e);
			throw new HostAuthenticationServiceException(msg, e);
		} catch (HostAuthenticationDaoException e) {
			String msg = supervisor == null
					? "Error reading pending security code for admin %d on host %s".formatted(admin.getAdminID(), hostID)
					: "Error reading pending security code for supervisor %d on host %s".formatted(supervisor.getId(), hostID);

			logger.error(msg, e);
			throw new HostAuthenticationServiceException(msg, e);
		}
	}

	@Override
	public void removeAllExpiredData() {
		logger.info("Removing expired data for host authentications (pending security codes and host authentications).");

		this.hostAuthenticationDao.removeExpiredHostAuthentications();
		this.hostAuthenticationDao.removeExpiredPendingAuthentications(configService.getMaxPendingHostAuthenticationsAgeMinutes());
	}

	@Override
	public void removeAuthentictedHost(final String hostId) {
		this.hostAuthenticationDao.removeAuthenticatedHost(hostId);
	}

	// ----------------------------------------------------------------------------------------------
	// Dependency Injection

	public void setConfigService(ConfigService service) {
		this.configService = service;
	}

	public void setHostAuthenticationDao(HostAuthenticationDao dao) {
		this.hostAuthenticationDao = dao;
	}

	public void setSecurityCodeGenerator(HostAuthenticationSecurityCodeGenerator generator) {
		this.securityCodeGenerator = generator;
	}

	public void setSystemMailMessageService(SystemMailMessageService systemMailMessageService) {
		this.systemMailMessageService = systemMailMessageService;
	}

}
