/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.company.service.impl;

import java.util.List;
import java.util.Objects;

import org.agnitas.beans.Company;
import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComCompany;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;

public final class CompanyServiceImpl implements CompanyService {
	
	private ComCompanyDao companyDao;
	
	@Override
	public final Company getCompanyOrNull(@VelocityCheck int companyId) {
		return companyDao.getCompany(companyId);
	}
	
	@Override
	public final ComCompany getCompany(@VelocityCheck int companyId) throws UnknownCompanyIdException {
		final ComCompany company = this.companyDao.getCompany(companyId);
		
		if(company == null) {
			throw new UnknownCompanyIdException(companyId);
		}
		
		return company;
	}

	@Override
	public boolean isCompanyExisting(@VelocityCheck int companyId) {
		return companyId > 0 && companyDao.isCompanyExist(companyId);
	}

	@Override
	public List<CompanyEntry> getActiveCompanyEntries() {
		return companyDao.getActiveCompaniesLight();
	}
	
	@Override
	public List<CompanyEntry> getActiveOwnCompanyEntries(@VelocityCheck int companyId) {
		return companyDao.getActiveOwnCompaniesLight(companyId);
	}

	@Required
	public final void setCompanyDao(final ComCompanyDao dao) {
		this.companyDao = Objects.requireNonNull(dao, "Company DAO cannot be null");
	}
}
