/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.service.impl;

import java.util.Date;
import java.util.List;

import com.agnitas.service.ExtendedConversionService;
import org.agnitas.beans.FailedLoginData;
import org.agnitas.dao.LoginTrackDao;
import org.agnitas.emm.core.logintracking.bean.LoginData;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import org.agnitas.emm.core.logintracking.service.LoginTrackServiceException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link LoginTrackService}.
 */
public class LoginTrackServiceBasicImpl implements LoginTrackService {
	
	/**
	 * The logger.
	 */
	private static final transient Logger logger = Logger.getLogger(LoginTrackServiceBasicImpl.class);
	
	// -------------------------------------------------------------------------------------------------- Business Code
	@Override
	public void trackLoginSuccessful(String ipAddress, String username) {
		this.loginTrackDao.trackSuccessfulLogin(ipAddress, username);
	}
	
	@Override
	public void trackLoginFailed(String ipAddress, String username) {
		this.loginTrackDao.trackFailedLogin(ipAddress, username);
	}
	
	@Override
	public void trackLoginSuccessfulButBlocked(String ipAddress, String username) {
		this.loginTrackDao.trackLoginDuringBlock(ipAddress, username);
	}
	
	
	@Override
	public int getNumFailedLoginsSinceLastSuccessful(String username, boolean skipLastSuccess) throws LoginTrackServiceException {
		try {
			if (logger.isInfoEnabled()) {
				logger.info("Get number of failed logins since last successful login of user " + username);
			}
			
			LoginData lastSuccessful = this.loginTrackDao.getLastSuccessfulLogin(username, skipLastSuccess);
			Date since = lastSuccessful != null ? lastSuccessful.getLoginTime() : null;
			
			if (logger.isDebugEnabled()) {
				if (since == null) {
					logger.debug("No last successful login found");
				} else {
					logger.debug("Last successful login was on " + since);
				}
			}
			return loginTrackDao.countFailedLogins(username, since);
		} catch (Exception e) {
			logger.error("Cannot check for failed logins (user: " + username + ")", e);
			
			throw new LoginTrackServiceException("Error check for failed logins", e);
		}
	}
	
	@Override
	public List<LoginData> getLoginData(String username, Date since) throws LoginTrackServiceException {
		try {
			if (logger.isInfoEnabled()) {
				logger.info("Listing login attempts for user " + username + " since " + since);
			}
			
			return this.loginTrackDao.getLoginAttemptsSince(username, since);
		} catch (Exception e) {
			logger.warn("Cannot list login attempts", e);
			
			throw new LoginTrackServiceException("Error listing login attempts", e);
		}
	}
	
	@Override
	public LoginData getLastSuccessfulLogin(String username, boolean skipLastSuccess) throws LoginTrackServiceException {
		try {
			return this.loginTrackDao.getLastSuccessfulLogin(username, skipLastSuccess);
		} catch (Exception e) {
			logger.error("Cannot read last successful login", e);
			
			throw new LoginTrackServiceException("Error reading last successful login", e);
		}
	}
	
	@Override
	public boolean isIPLogonBlocked(String hostIpAddress, int maxLoginFails, int loginBlockTime) {
		FailedLoginData data = loginTrackDao.getFailedLoginData(hostIpAddress);
		
		if (data.getNumFailedLogins() > maxLoginFails) {
			return data.getLastFailedLoginTimeDifference() < loginBlockTime;
		} else {
			return false;
		}
	}
	
	
	// -------------------------------------------------------------------------------------------------- Dependency Injection
	/**
	 * DAO for login tracking.
	 */
	private LoginTrackDao loginTrackDao;
	
	protected ExtendedConversionService conversionService;
	
	/**
	 * Set DAO for login tracking.
	 *
	 * @param dao DAO for login tracking
	 */
	@Required
	public void setLoginTrackDao(LoginTrackDao dao) {
		this.loginTrackDao = dao;
	}
	
	@Required
	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}
}
