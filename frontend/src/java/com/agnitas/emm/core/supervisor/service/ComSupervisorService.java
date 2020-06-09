/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.service;

import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.common.SupervisorSortCriterion;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.emm.util.SortDirection;
import com.agnitas.web.mvc.Popups;

/**
 * Service interface for accessing supervisor data.
 */
public interface ComSupervisorService {
	
	/**
	 * List all supervisors. List is sorted according to given parameters.
	 * 
	 * @param criterion sorting criterion
	 * @param direction sorting direction
	 * 
	 * @return list of all supervisors
	 * 
	 * @throws SupervisorException on errors listing all supervisors
	 */
	List<Supervisor> listSupervisors(SupervisorSortCriterion criterion, SortDirection direction) throws SupervisorException;

	/**
	 * Returns supervisor for given ID. 
	 * 
	 * @param id ID of supervisor
	 * 
	 * @return supervisor for given ID.
	 * 
	 * @throws SupervisorException on errors reading supervisor
	 */
	Supervisor getSupervisor(int id) throws SupervisorException;

	/**
	 * Set new supervisor password.
	 * 	
	 * @param id ID of supervisor
	 * @param password new password
	 * 
	 * @throws SupervisorException on errors settings supervisor password
	 */
	void setSupervisorPassword(int id, String password) throws SupervisorException;

	boolean save(Supervisor supervisor, String password, List<Integer> allowedCompanyIds, Popups popups);

	/**
	 * Determines expiration state of supervisor password.
	 * A password can either be valid, nearly expired or expired.
	 * 
	 * @param supervisor {@link Supervisor} to check password
	 * 
	 * @return state of password
	 */
	PasswordState getPasswordState(Supervisor supervisor);

	/**
	 * Computes date of password expiration for given supervisor.
	 * 
	 * @param supervisor supervisor
	 * 
	 * @return date of password expiration for given supervisor
	 */
	Date computePasswordExpireDate(Supervisor supervisor);

	Date computePasswordFinalExpireDate(Supervisor supervisor);

	/**
	 * Checks, if given password is current password of supervisor.
	 * 
	 * @param supervisor {@link Supervisor}
	 * @param pwd password to check
	 * 
	 * @return {@code true} if given password is current password
	 * 
	 * @throws SupervisorException on errors checking password
	 */
	boolean isCurrentPassword(Supervisor supervisor, String pwd) throws SupervisorException;

	List<Integer> getAllowedCompanyIds(int supervisorId);
	
	boolean deleteSupervisor(int id);
}
