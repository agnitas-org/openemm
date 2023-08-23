/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.company.service;

import java.util.List;

import com.agnitas.beans.Company;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;

public interface CompanyService {
	
	Company getCompanyOrNull(int companyId);

	Company getCompany(int companyId) throws UnknownCompanyIdException;

	boolean isCompanyExisting(int companyId);

	//get all active companies
	List<CompanyEntry> getActiveCompanyEntries(boolean allowTransitionStatus);
	
	//get only own company and companies created by own company + status = active
	List<CompanyEntry> getActiveOwnCompanyEntries(int companyId, boolean allowTransitionStatus);

	List<Company> getCreatedCompanies(int companyId);
	
}
