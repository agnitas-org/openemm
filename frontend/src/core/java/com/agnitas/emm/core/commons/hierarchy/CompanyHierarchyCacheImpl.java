/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.hierarchy;

import java.util.Objects;

import com.agnitas.util.TimeoutLRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Company;
import com.agnitas.dao.CompanyDao;

public class CompanyHierarchyCacheImpl implements CompanyHierarchyCache {
	
	private static final Logger logger = LogManager.getLogger( CompanyHierarchyCacheImpl.class);

	/** Default retention time in milliseconds ({@value #DEFAULT_TIMEOUT_MILLIS} ms). */
	public static final int DEFAULT_TIMEOUT_MILLIS = 300000; // 5 minutes
	
	/** Default capacity of cache ({@value #DEFAULT_CAPACITY} items). */
	public static final int DEFAULT_CAPACITY = 1000;
	
	/** Caching map. */
	private final TimeoutLRUMap<Integer, Integer> companyToRootCompanyMap;
	
	/** Company DAO to read missing data into cache. */
	private final CompanyDao companyDao;

	public CompanyHierarchyCacheImpl(final CompanyDao companyDao) {
		this(DEFAULT_TIMEOUT_MILLIS, DEFAULT_CAPACITY, companyDao);
	}
	
	public CompanyHierarchyCacheImpl(final int timeoutMillis, final int capacity, final CompanyDao companyDao) {
		this.companyToRootCompanyMap = new TimeoutLRUMap<>(capacity, timeoutMillis);
		this.companyDao = Objects.requireNonNull(companyDao, "company DAO");
	}
	
	@Override
	public int getRootCompanyId(final int companyId) {
		logger.info("Determining root company for company ID {}", companyId);

		if( this.companyToRootCompanyMap.containsKey( companyId)) {
			int root = this.companyToRootCompanyMap.get( companyId);
			
			logger.debug("Root company for company ID {} is {}", companyId, root);

			return root;
		} else {
			int companyIdToCheck = companyId;
			
			Company company = this.companyDao.getCompany( companyIdToCheck);
			
			if( company == null)
				return 0;

			if( company.getParentCompanyId() == 0 || company.getParentCompanyId() == company.getId()) {
				logger.debug("No parent company set, so company {} is root company", company.getId());
				// No parent, so the company is its own root
				companyToRootCompanyMap.put( company.getId(), company.getId());
				
				return company.getId();
			} else {
				logger.debug("Found parent company {} for company {} - checking that parent company", company.getParentCompanyId(), company.getId());

				int rootId = getRootCompanyId( company.getParentCompanyId());
				
				logger.debug("Got root company {} for parent company {} of company {}", rootId, company.getParentCompanyId(), company.getId());

				companyToRootCompanyMap.put( company.getId(), rootId);
				
				return rootId;
			}
				
		}
	}

}
