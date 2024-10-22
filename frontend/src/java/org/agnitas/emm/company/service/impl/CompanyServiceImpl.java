/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.company.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.agnitas.beans.Company;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;
import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

public final class CompanyServiceImpl implements CompanyService {
	
	private ComCompanyDao companyDao;

	@Override
	public boolean isMailtrackingActive(int companyId) {
		return companyDao.isMailtrackingActive(companyId);
	}

	@Override
	public Set<String> getTechnicalContacts(int companyId) {
		String technicalContact = StringUtils.defaultString(companyDao.getTechnicalContact(companyId));
		return new HashSet<>(AgnUtils.splitAndTrimList(technicalContact));
	}

	@Override
	public Company getCompanyOrNull(int companyId) {
		return companyDao.getCompany(companyId);
	}
	
	@Override
	public Company getCompany(int companyId) throws UnknownCompanyIdException {
		final Company company = this.companyDao.getCompany(companyId);
		
		if(company == null) {
			throw new UnknownCompanyIdException(companyId);
		}
		
		return company;
	}

	@Override
	public boolean isCompanyExisting(int companyId) {
		return companyId > 0 && companyDao.isCompanyExist(companyId);
	}

	@Override
	public List<Integer> getCompaniesIds() {
		return companyDao.getCompaniesIds();
	}

	@Override
	public List<CompanyEntry> getActiveCompanyEntries(boolean allowTransitionStatus) {
		return companyDao.getActiveCompaniesLight(allowTransitionStatus);
	}
	
	@Override
	public List<CompanyEntry> getActiveOwnCompanyEntries(int companyId, boolean allowTransitionStatus) {
		return companyDao.getActiveOwnCompaniesLight(companyId, allowTransitionStatus);
	}

	@Override
	public List<Company> getCreatedCompanies(int companyId) {
		return companyDao.getCreatedCompanies(companyId);
	}

	@Override
	public List<CompanyEntry> findAllByEmailPart(String email, int companyID) {
		return companyDao.findAllByEmailPart(email, companyID);
	}

	@Override
	public List<CompanyEntry> findAllByEmailPart(String email) {
		return companyDao.findAllByEmailPart(email);
	}

	@Override
	public void updateTechnicalContact(String email, int id) {
		companyDao.updateTechnicalContact(email, id);
	}

	@Required
	public void setCompanyDao(final ComCompanyDao dao) {
		this.companyDao = Objects.requireNonNull(dao, "Company DAO cannot be null");
	}
}
