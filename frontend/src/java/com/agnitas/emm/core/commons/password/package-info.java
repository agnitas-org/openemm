/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/**
 * This package contains EMM-specific classes dealing with {@link org.agnitas.beans.Admin}- and
 * {@link com.agnitas.emm.core.supervisor.beans.Supervisor}-passwords.
 * 
 * To check, if a password satisfies security constraints, simply call
 * <ul>
 *   <li>{@link com.agnitas.emm.core.commons.password.ComPasswordCheck#checkAdminPassword(String, org.agnitas.beans.Admin, org.agnitas.emm.core.commons.password.PasswordCheckHandler)} or</li>
 *   <li>{@link com.agnitas.emm.core.commons.password.ComPasswordCheck#checkSupervisorPassword(String, com.agnitas.emm.core.supervisor.beans.Supervisor, org.agnitas.emm.core.commons.password.PasswordCheckHandler)}
 * </ul>
 * 
 * @see org.agnitas.emm.core.commons.password
 */
package com.agnitas.emm.core.commons.password;

