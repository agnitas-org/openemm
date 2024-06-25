/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.daocache.impl;

import java.util.Objects;

import org.agnitas.emm.core.commons.daocache.AbstractDaoCache;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.TimeoutLRUMap;

import com.agnitas.beans.Company;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.commons.uid.beans.CompanyUidData;

public class CompanyUidDataDaoCacheImpl extends AbstractDaoCache<CompanyUidData> implements CompanyUidDataDaoCache {

	private final ConfigService configService;
	private final ComCompanyDao companyDao;
	
	public CompanyUidDataDaoCacheImpl(final ComCompanyDao companyDao, final ConfigService configService) {
		this.configService = Objects.requireNonNull(configService, "configService");
		this.companyDao = Objects.requireNonNull(companyDao, "companyDao");
	}
	
	@Override
	protected CompanyUidData getItemFromDao(int id) {
		if (!isCacheInitialized()) {
			setCache(new TimeoutLRUMap<>(
				configService.getIntegerValue(ConfigValue.CompanyMaxCache),
				configService.getIntegerValue(ConfigValue.CompanyMaxCacheTimeMillis)));
		}
		
		final Company company = companyDao.getCompany(id);
		
		return company != null ? CompanyUidData.from(company) : null;
	}

}
