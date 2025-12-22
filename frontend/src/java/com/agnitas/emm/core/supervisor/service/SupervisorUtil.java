/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.supervisor.beans.Supervisor;

/**
 * Utility methods dealing with {@link Supervisor}.
 */
public class SupervisorUtil {
	
	/** Character to separate supervisor names from user names. */
	public static final char SUPERVISOR_SEPARATOR_CHAR = '/';
	
	/**
	 * Checks if login is attempt with supervisor name. 
	 * 
	 * @param username name to check
	 * 
	 * @return {@code true} if login is supervisor login
	 */
	public static boolean isSupervisorLoginName(String username) {
		return username.indexOf(SUPERVISOR_SEPARATOR_CHAR) != -1;
	}

	/**
	 * Returns the user name from login name. If login name does not contain the separator character,
	 * the complete login name is returned.
	 * 
	 * @param loginName name used at login
	 * 
	 * @return user name
	 */
	public static String getSupervisorNameFromLoginName(String loginName) {
		int index = loginName.indexOf(SUPERVISOR_SEPARATOR_CHAR);
		
		if( index != -1) {
			return loginName.substring( index + 1).trim();
		} else {
			return loginName;
		}
	}
	
	/**
	 * Returns the supervisor name from login name. If login name does not contain the separator character,
	 * {@code null} is returned.
	 * 
	 * @param loginName name used at login
	 * 
	 * @return supervisor name or {@code null}
	 */
	public static String getUserNameFromLoginName(String loginName) {
		int index = loginName.indexOf(SUPERVISOR_SEPARATOR_CHAR);
		
		if( index != -1) {
			return loginName.substring( 0, index).trim();
		} else {
			return loginName;
		}
	}

	/**
	 * Formats user and supervisor name to complete login name.
	 *  
	 * @param username user name
	 * @param supervisorName supervisor name
	 * 
	 * @return complete login name
	 */
	public static String formatCompleteName(String username, String supervisorName) {
		if( supervisorName == null) {
			return username;
		} else { 
			return username + SUPERVISOR_SEPARATOR_CHAR + supervisorName;
		}
	}

	public static String formatCompleteName(Admin admin) {
		if (!admin.isSupervisor()) {
			return admin.getUsername();
		}

		return formatCompleteName(admin.getUsername(), admin.getSupervisor().getSupervisorName());
	}

	/**
	 * If admin supports supervisors, the supervisor is returned (if set). If admin does not support supervisors,
	 * null is returned.
	 *  
	 * @param admin admin to get supervisor from
	 * 
	 * @return {@link Supervisor} or null
	 */
	public static Supervisor extractSupervisor(Admin admin) {
		return admin.getSupervisor();
	}
}
