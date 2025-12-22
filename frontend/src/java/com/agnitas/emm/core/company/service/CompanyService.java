/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.service;

import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.Company;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.impl.CompanyStatus;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.company.dto.CompanyInfoDto;
import com.agnitas.emm.core.company.form.CompanyCreateForm;
import com.agnitas.emm.core.company.form.CompanyViewForm;
import com.agnitas.web.forms.PaginationForm;
import com.agnitas.web.mvc.Popups;

public interface CompanyService {

	List<Company> listActiveCompanies();

	Company getCompany(int companyID);

	CompanyInfoDto getLight(int companyID);

	int getPriorityCount(int companyId);

	void setPriorityCount(int companyId, int value);

    Set<Permission> getCompanyPermissions(int companyID);

	CompanyViewForm getCompanyForm(int companyId);

	List<AdminEntry> getAdmins(int companyId);

	PaginatedList<AdminEntry> getAdmins(PaginationForm form, int companyId);

	int create(Admin admin, CompanyCreateForm form, Popups popups, String sessionId) throws Exception;	// TODO Replace CompanyCreateForm. This class belongs to presentation layer only.

	int update(Admin admin, CompanyViewForm form);	// TODO Replace CompanyViewForm. This class belongs to presentation layer only.

	boolean markCompanyForDeletion(final int companyId);

	boolean isCompanyNameUnique(int companyId, String shortname);

	boolean isCreatorId(int companyId, int creatorId);
	
	boolean createFrequencyFields(int companyID);
	
	CompanyStatus getStatus(int companyID);
	
	int getCompanyDatasource(int companyId);

	int getNumberOfCompanies();

    String getTechnicalContact(int companyId);

	boolean isCompanyExisting(int companyId);

	//get all active companies
	List<CompanyEntry> getActiveCompanyEntries(boolean allowTransitionStatus);

	//get only own company and companies created by own company + status = active
	List<CompanyEntry> getActiveOwnCompanyEntries(int companyId, boolean allowTransitionStatus);

	List<Company> getCreatedCompanies(int companyId);

	List<CompanyEntry> findAllByEmailPart(String email, int companyID);

	List<CompanyEntry> findAllByEmailPart(String email);

	void updateEmails(String techContactEmail, Set<String> systemMessageEmails, int id);

	List<Integer> getCompaniesIds();

	boolean isMailtrackingActive(int companyId);

}
