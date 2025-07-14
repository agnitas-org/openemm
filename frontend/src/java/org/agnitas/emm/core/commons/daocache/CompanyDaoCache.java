/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.daocache;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.TimeoutLRUMap;
import com.agnitas.beans.Company;
import com.agnitas.dao.CompanyDao;

/**
 * Implementation of DAOCache for wrapping a CompanyDAO.
 */
public class CompanyDaoCache extends AbstractDaoCache<Company> {

	// --------------------------------------------------- Dependency Injection
	/** Wrapped CompanyDAO. */
	private CompanyDao companyDao;

	private ConfigService configService;

	/**
	 * Set the company DAO.
	 * 
	 * @param companyDao company DAO
	 */
	public void setCompanyDao(CompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Override
	protected Company getItemFromDao( int id) {
		if (!isCacheInitialized()) {
			setCache(new TimeoutLRUMap<>(
				configService.getIntegerValue(ConfigValue.CompanyMaxCache),
				configService.getIntegerValue(ConfigValue.CompanyMaxCacheTimeMillis)));
		}
		return companyDao.getCompany( id);
	}
}
