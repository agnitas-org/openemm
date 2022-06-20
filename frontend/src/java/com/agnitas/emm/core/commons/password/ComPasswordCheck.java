/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.password;

import org.agnitas.emm.core.commons.password.PasswordCheck;
import org.agnitas.emm.core.commons.password.PasswordCheckHandler;

import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.service.SimpleServiceResult;

/**
 * Extension of {@link PasswordCheck} interface to deal with {@link Supervisor}s.
 */
public interface ComPasswordCheck extends PasswordCheck {
	
	/**
	 * Check supervisor password.
	 * 
	 * @param password password to check
	 * @param supervisor {@link Supervisor} used for comparison of passwords
	 * @param handler error handler
	 * 
	 * @return {@code true} if password is ok, otherwise {@code false}
	 * 
	 * @throws SupervisorException on errors accessing supervisor data
	 * 
	 * @see PasswordCheck#checkAdminPassword(String, com.agnitas.beans.ComAdmin, PasswordCheckHandler)
	 */
	boolean checkSupervisorPassword(String password, Supervisor supervisor, PasswordCheckHandler handler) throws SupervisorException;

	/**
	 * Check supervisor password.
	 *
	 * @param password password to check
	 * @param supervisor {@link Supervisor} used for comparison of passwords
	 *
	 * @return a {@link SimpleServiceResult} instance.
	 *
	 * @throws SupervisorException on errors accessing supervisor data.
	 */
	SimpleServiceResult checkSupervisorPassword(String password, Supervisor supervisor) throws SupervisorException;

	/**
	 * Check supervisor password.
	 *
	 * @param password password to check
	 * @param handler error handler
	 *
	 * @return {@code true} if password is ok, otherwise {@code false}
	 *
	 * @see PasswordCheck#checkAdminPassword(String, com.agnitas.beans.ComAdmin, PasswordCheckHandler)
	 */
	boolean checkSupervisorPassword(String password, PasswordCheckHandler handler);

}
