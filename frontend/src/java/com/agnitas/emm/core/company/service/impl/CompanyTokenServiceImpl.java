/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.service.impl;

import java.util.Objects;
import java.util.Optional;

import com.agnitas.beans.Company;
import com.agnitas.emm.core.commons.tokengen.TokenGenerator;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.company.dao.CompanyTokenDao;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.company.service.FailedToAssignCompanyTokenException;
import org.springframework.transaction.annotation.Transactional;

public class CompanyTokenServiceImpl implements CompanyTokenService {

	private CompanyTokenDao companyTokenDao;
	private CompanyService companyService;
	private TokenGenerator tokenGenerator;
	private ConfigService configService;
	
	@Override
	public Optional<Company> findCompanyByToken(String token) {
		return companyTokenDao.getCompanyIdByToken(token)
				.map(cId -> companyService.getCompany(cId));
	}

	@Override
	public Optional<Integer> findCompanyIdByToken(String token) {
		return companyTokenDao.getCompanyIdByToken(token);
    }

	@Override
	public Optional<String> getCompanyToken(int companyID) {
		return this.companyTokenDao.getCompanyToken(companyID);
	}

	@Transactional
	@Override
	public void assignRandomToken(int companyID) {
        if (getCompanyToken(companyID).isPresent()) {
			return;
		}

        // Up to 100 attempts to find a free token
        for(int i = 0; i < 100; i++) {
            String token = tokenGenerator.generateToken(configService.getIntegerValue(ConfigValue.CompanyTokenLength, companyID));

            if (!isTokenInUse(token)) {
                this.companyTokenDao.assignToken(companyID, token);
                return;
            }
        }

        throw new FailedToAssignCompanyTokenException(companyID, "Could not find a free token");
    }
	
	private boolean isTokenInUse(String token) {
		return findCompanyIdByToken(token).isPresent();
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
