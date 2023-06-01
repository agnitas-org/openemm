/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.service;

import java.util.List;
import java.util.Set;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.CompanyStatus;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.password.policy.PasswordPolicies;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Company;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.company.dto.CompanyInfoDto;
import com.agnitas.emm.core.company.form.CompanyCreateForm;
import com.agnitas.emm.core.company.form.CompanyViewForm;
import com.agnitas.web.mvc.Popups;

public interface ComCompanyService {

    boolean initTables(int companyID);

	List<Company> listActiveCompanies();

	Company getCompany(int companyID);

	CompanyInfoDto getLight(int companyID);

	boolean addExecutiveAdmin(int companyID, int executiveAdminID);

	int getPriorityCount(@VelocityCheck int companyId);

	void setPriorityCount(@VelocityCheck int companyId, int value);

    Set<Permission> getCompanyPermissions(int companyID);

    PaginatedListImpl<CompanyInfoDto> getCompanyList(@VelocityCheck int companyID, String sort, String direction, int page, int rownums);

	CompanyViewForm getCompanyForm(int companyId);

	List<AdminEntry> getAdmins(int companyId);

	int create(Admin admin, CompanyCreateForm form, Popups popups, String sessionId) throws Exception;

	int update(Admin admin, CompanyViewForm form) throws Exception;

    boolean deleteCompany(int companyIdForRemove);
    
    boolean deactivateCompany(int companyIdForDeactivation);

	boolean isCompanyNameUnique(int companyId, String shortname);

	boolean isCreatorId(@VelocityCheck int companyId, int creatorId);
	
	boolean createFrequencyFields(@VelocityCheck int companyID);
	
	CompanyStatus getStatus(int companyID);
	
	/**
	 * Returns the password policy for given company ID.
	 * 
	 * @param companyID company ID
	 * 
	 * @return password policy for company ID
	 */
	public PasswordPolicies getPasswordPolicy(@VelocityCheck final int companyID);

	boolean reactivateCompany(int companyIdForDeactivation);

	int getCompanyDatasource(@VelocityCheck int companyId);

	int getNumberOfCompanies();
}
