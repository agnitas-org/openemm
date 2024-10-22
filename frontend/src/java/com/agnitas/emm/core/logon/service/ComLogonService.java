/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service;

import java.util.Date;
import java.util.Set;

import com.agnitas.beans.EmmLayoutBase;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminPreferences;
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
	 * Returns {@link Admin} for given user name and password. A {@link LogonFailedException}
	 * is thrown when logon failed for some reason (invalid combination of user name and
	 * password, account blocked, ...)
	 * 
	 * Login attempt is logged with its result.
	 * 
	 * @param username user name
	 * @param password password
	 * @param hostIpAddress IP address of host
	 * @return {@link Admin}
	 * 
	 * @throws LogonFailedException if there is no user for given credentials
	 * @throws LogonServiceException on errors during login
	 */
	Admin getAdminByCredentials(String username, String password, String hostIpAddress) throws LogonServiceException;

	Admin getAdminByUsername(String username);

	ServiceResult<Admin> authenticate(String username, String password, String clientIp);

	SimpleServiceResult checkDatabase();

	AdminPreferences getPreferences(Admin admin);

	EmmLayoutBase getEmmLayoutBase(Admin admin);

	String getLayoutDirectory(String serverName);

	String getHelpLanguage(Admin admin);

	PasswordState getPasswordState(Admin admin);

	Date getPasswordExpirationDate(Admin admin);

	SimpleServiceResult setPassword(Admin admin, String password);

	SimpleServiceResult requestPasswordReset(String username, String email, String clientIp, String linkPattern);

    ServiceResult<Admin> resetPassword(String username, String token, String password, String clientIp);
    
    String getPasswordResetLink(String linkPattern, String username, String token);
    
    SimpleServiceResult sendWelcomeMail(Admin admin, String clientIp, String linkPattern);
	void sendWelcomeMail(Set<Integer> ids, String clientIp, int companyID, String passwordResetLinkPattern);

	boolean existsPasswordResetTokenHash(String username, String token);

	boolean isValidPasswordResetTokenHash(String username, String token);

	void riseErrorCount(String username);

	void updateSessionsLanguagesAttributes(Admin admin);
}
