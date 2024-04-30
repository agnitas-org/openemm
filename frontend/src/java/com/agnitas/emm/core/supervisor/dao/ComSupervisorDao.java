/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.dao;

import java.util.List;

import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.emm.core.supervisor.common.SupervisorSortCriterion;
import com.agnitas.emm.util.SortDirection;

/**
 * Interface for accessing supervisor data.
 */
public interface ComSupervisorDao {

	/**
	 * Returns {@link Supervisor} for given credentials.
	 *
	 * @param supervisorName name of supervisor
	 * @param password password
	 *
	 * @return {@link Supervisor} for credentials
	 *
	 * @throws SupervisorException on errors reading supervisor (invalid username/password, ...)
	 */
	Supervisor getSupervisor(String supervisorName, String password) throws SupervisorException;

	/**
	 * List all supervisors (active and inactive). List is sorted according to given parameters.
	 *
	 * @param criterion sorting criterion (full name, supervisor name, ...)
	 * @param direction sorting direction (ascending, descending)
	 *
	 * @return list of all supervisors
	 *
	 * @throws SupervisorException on errors listing supervisors
	 */
	List<Supervisor> listAllSupervisors(SupervisorSortCriterion criterion, SortDirection direction) throws SupervisorException;
	
	/**
	 * Returns supervisor by ID.
	 *
	 * @param id ID of supervisor
	 *
	 * @return supervisor for given ID
	 *
	 * @throws SupervisorException on errors accessing supervisor
	 */
	Supervisor getSupervisor(int id) throws SupervisorException;

	/**
	 * Set new supervisor password.public boolean updateSupervisorBindings(Supervisor supervisor);
	 *
	 * @param id ID of supervisor
	 * @param password new password
	 *
	 * @throws SupervisorException on errors setting new password
	 */
	void setSupervisorPassword(int id, String password) throws SupervisorException;

	/**
	 * Checks, if given password is current password of supervisor.
	 *
	 * @param id ID of supervisor
	 * @param pwd password to check
	 *
	 * @return {@code true} if given password is current password
	 *
	 * @throws SupervisorException on errors checking password
	 */
	boolean isCurrentPassword(int id, String pwd) throws SupervisorException;

	Supervisor getSupervisor(String supervisorName);
	
	int getNumberOfSupervisors();
	
	/**
	 * Creates a new supervisor by given data
	 *
	 * @param supervisor
	 * @return new supervisorID
	 */
	int createSupervisor(Supervisor supervisor);
    
    List<Integer> getAllowedCompanyIDs(int supervisorId);

	void setAllowedCompanyIds(int id, List<Integer> allowedCompanyIds) throws SupervisorException;

	Supervisor updateSupervisor(Supervisor supervisor);

	boolean logSupervisorLogin(int supervisorId, int companyId);
	
	void cleanupUnusedSupervisorBindings(int daysBeforeInactive);
	
	boolean deleteSupervisor(int supervisorId);

	boolean existsSupervisor(String supervisorName);
}
