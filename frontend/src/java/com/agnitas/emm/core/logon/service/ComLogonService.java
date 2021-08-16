/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service;

import java.util.Date;

import org.agnitas.beans.EmmLayoutBase;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.logon.web.LogonFailedException;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;

/**
 * Interface for logon process.
 */
public interface ComLogonService {
	public static final String DEFAULT_HELP_LANGUAGE = "en";
	public static final int TOKEN_EXPIRATION_MINUTES = 30;
	public static final int TOKEN_EXPIRATION_DAYS = 3;
	
	/**
	 * Returns {@link ComAdmin} for given user name and password. A {@link LogonFailedException}
	 * is thrown when logon failed for some reason (invalid combination of user name and
	 * password, account blocked, ...)
	 * 
	 * Login attempt is logged with its result.
	 * 
	 * @param username user name
	 * @param password password
	 * @param hostIpAddress IP address of host
	 * @return {@link ComAdmin}
	 * 
	 * @throws LogonFailedException if there is no user for given credentials
	 * @throws LogonServiceException on errors during login
	 */
	ComAdmin getAdminByCredentials(String username, String password, String hostIpAddress) throws LogonServiceException;

	ServiceResult<ComAdmin> authenticate(String username, String password, String clientIp);

	SimpleServiceResult checkDatabase();

	ComAdminPreferences getPreferences(ComAdmin admin);

	EmmLayoutBase getEmmLayoutBase(ComAdmin admin);

	String getLayoutDirectory(String serverName);

	String getHelpLanguage(ComAdmin admin);

	PasswordState getPasswordState(ComAdmin admin);

	Date getPasswordExpirationDate(ComAdmin admin);

	SimpleServiceResult setPassword(ComAdmin admin, String password);

	SimpleServiceResult requestPasswordReset(String username, String email, String clientIp, String linkPattern);

    ServiceResult<ComAdmin> resetPassword(String username, String token, String password, String clientIp);
    
    String getPasswordResetLink(String linkPattern, String username, String token);
    
    SimpleServiceResult sendWelcomeMail(ComAdmin admin, String clientIp, String linkPattern);

	boolean existsPasswordResetTokenHash(String username, String token);

	boolean isValidPasswordResetTokenHash(String username, String token);

	void riseErrorCount(String username);

	void updateSessionsLanguagesAttributes(ComAdmin admin);
}
