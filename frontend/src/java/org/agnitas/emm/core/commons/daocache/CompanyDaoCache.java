/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.daocache;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.TimeoutLRUMap;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Company;
import com.agnitas.dao.ComCompanyDao;

/**
 * Implementation of DAOCache for wrapping a CompanyDAO.
 */
public class CompanyDaoCache extends AbstractDaoCache<Company> {

	// --------------------------------------------------- Dependency Injection
	/** Wrapped CompanyDAO. */
	private ComCompanyDao companyDao;

	private ConfigService configService;

	/**
	 * Set the company DAO.
	 * 
	 * @param companyDao company DAO
	 */
	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Override
	protected Company getItemFromDao( int id) {
		if (!isCacheInitialized()) {
			setCache(new TimeoutLRUMap<Integer, Company>(
				configService.getIntegerValue(ConfigValue.CompanyMaxCache),
				configService.getIntegerValue(ConfigValue.CompanyMaxCacheTimeMillis)));
		}
		return companyDao.getCompany( id);
	}
}
