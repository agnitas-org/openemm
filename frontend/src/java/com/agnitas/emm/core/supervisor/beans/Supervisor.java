/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.beans;

import java.util.Date;

import com.agnitas.emm.core.departments.beans.Department;

/**
 * Interface for supervisor data.
 * 
 * Note: This interface does not provide access to password data!
 */
public interface Supervisor {
	String ALL_COMPANIES = "all"; //access to all companies
 
	/**
	 * Returns ID of supervisor.
	 * 
	 * @return ID of supervisor
	 */
	int getId();
	
	/**
	 * Set ID of supervisor.
	 * 
	 * @param id ID of supervisor
	 */
	void setId( int id);
	
	/**
	 * Returns supervisor name.
	 * 
	 * @return supervisor name
	 */
	String getSupervisorName();
	
	/**
	 * Set supervisor name.
	 * 
	 * @param name supervisor name
	 */
	void setSupervisorName( String name);
	
	/**
	 * Returns real name of supervisor.
	 * 
	 * @return real name of supervisor
	 */
	String getFullName();
	
	/**
	 * Sets real name of supervisor.
	 * 
	 * @param name real name of supervisor
	 */
	void setFullName( String name);
	
	/**
	 * Returns state (active / inactive) of supervisor.
	 * 
	 * @return {@code true} if supervisor is active
	 */
	boolean isActive();
	
	/**
	 * Set active state of supervisor. If supervisor is active set to {@code true}.
	 * 
	 * @param active {@code true} if supervisor is active
	 */
	void setActive(boolean active);

	/**
	 * Set date of last password change.
	 * 
	 * @param date date of last password change
	 */
	void setLastPasswordChangeDate(Date date);
	
	/**
	 * Return date of last password change.
	 * 
	 * @return date of last password change
	 */
	Date getLastPasswordChangeDate();
	
	/**
	 * Set email address of supervisor.
	 * 
	 * @param email email address
	 */
	void setEmail(String email);
	
	/**
	 * Get email address of supervisor.
	 * 
	 * @return email address of supervisor
	 */
	String getEmail();
	
	/**
	 * Set department of supervisor user.
	 * 
	 * @param department department
	 */
	void setDepartment(final Department department);
	
	/**
	 * Returns the department.
	 * Return value can be <code>null</code>.
	 * 
	 * @return department of supervisor or <code>null</code>
	 */
	Department getDepartment();
}
