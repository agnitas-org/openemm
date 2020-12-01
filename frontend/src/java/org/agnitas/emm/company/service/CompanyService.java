/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.company.service;

import java.util.List;

import com.agnitas.beans.ComCompany;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface CompanyService {
	
	ComCompany getCompanyOrNull(@VelocityCheck int companyId);

	ComCompany getCompany(@VelocityCheck int companyId) throws UnknownCompanyIdException;

	boolean isCompanyExisting(@VelocityCheck int companyId);

	//get all active companies
	List<CompanyEntry> getActiveCompanyEntries(boolean allowTransitionStatus);
	
	//get only own company and companies created by own company + status = active
	List<CompanyEntry> getActiveOwnCompanyEntries(@VelocityCheck int companyId, boolean allowTransitionStatus);

	List<ComCompany> getCreatedCompanies(@VelocityCheck int companyId);
	
}
