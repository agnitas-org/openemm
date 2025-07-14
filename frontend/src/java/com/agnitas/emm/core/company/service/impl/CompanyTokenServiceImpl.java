/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.service.impl;

import java.util.Objects;
import java.util.Optional;

import com.agnitas.emm.core.company.service.CompanyService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.Company;
import com.agnitas.emm.core.commons.tokengen.TokenGenerator;
import com.agnitas.emm.core.company.dao.CompanyTokenDao;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.company.service.FailedToAssignCompanyTokenException;
import com.agnitas.emm.core.company.service.UnknownCompanyTokenException;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;

public final class CompanyTokenServiceImpl implements CompanyTokenService {

	private static final Logger logger = LogManager.getLogger(CompanyTokenServiceImpl.class);

	private CompanyTokenDao companyTokenDao;
	private CompanyService companyService;
	private TokenGenerator tokenGenerator;
	private ConfigService configService;
	
	@Override
	public Company findCompanyByToken(final String token) throws UnknownCompanyTokenException {
        Company company = companyService.getCompany(companyTokenDao.getCompanyIdByToken(token));

		if (company == null) {
			logger.error("Company not found by token: {}", token);
			throw new UnknownCompanyTokenException(token);
		}

		return company;
	}

	@Override
	public Integer findCompanyIdToken(String token) {
        try {
            return findCompanyByToken(token).getId();
        } catch (UnknownCompanyTokenException e) {
			logger.error("Company not found by token: {}", token);
            return null;
        }
    }

	@Override
	public Optional<String> getCompanyToken(final int companyID) throws UnknownCompanyIdException {
		return this.companyTokenDao.getCompanyToken(companyID);
	}

	@Transactional
	@Override
	public final void assignRandomToken(final int companyID, final boolean overwriteExisting) throws UnknownCompanyIdException, FailedToAssignCompanyTokenException {
		final Optional<String> tokenOpt = getCompanyToken(companyID);

		if(!tokenOpt.isPresent() || overwriteExisting) {
			// Up to 100 attempts to find a free token
			for(int i = 0; i < 100; i++) {
				final String token = this.tokenGenerator.generateToken(this.configService.getIntegerValue(ConfigValue.CompanyTokenLength, companyID));
			
				if(!isTokenInUse(token)) {
					this.companyTokenDao.assignToken(companyID, token);
					return;
				}
			}
			
			throw new FailedToAssignCompanyTokenException(companyID, "Could not find a free token");
		}
	}
	
	private boolean isTokenInUse(final String token) {
		try {
			findCompanyByToken(token);
			
			return true;
		} catch(final UnknownCompanyTokenException e) {
			return false;
		}
	}

	public void setCompanyTokenDao(final CompanyTokenDao dao) {
		this.companyTokenDao = Objects.requireNonNull(dao, "CompanyTokenDao is null");
	}
	
	public void setCompanyService(final CompanyService service) {
		this.companyService = Objects.requireNonNull(service, "CompanyService is null");
	}
	
	public void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}
	
	public void setTokenGenerator(final TokenGenerator generator) {
		this.tokenGenerator = Objects.requireNonNull(generator, "TokenGenerator is null");
	}
}
