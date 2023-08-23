/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.hierarchy;

import java.util.Objects;

import org.agnitas.util.TimeoutLRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Company;
import com.agnitas.dao.ComCompanyDao;

public class CompanyHierarchyCacheImpl implements CompanyHierarchyCache {
	
	/** The logger. */
	private static final Logger logger = LogManager.getLogger( CompanyHierarchyCacheImpl.class);

	/** Default retention time in milliseconds ({@value #DEFAULT_TIMEOUT_MILLIS} ms). */
	public static final int DEFAULT_TIMEOUT_MILLIS = 300000; // 5 minutes
	
	/** Default capacity of cache ({@value #DEFAULT_CAPACITY} items). */
	public static final int DEFAULT_CAPACITY = 1000;
	
	/** Caching map. */
	private final TimeoutLRUMap<Integer, Integer> companyToRootCompanyMap;
	
	/** Company DAO to read missing data into cache. */
	private final ComCompanyDao companyDao;

	public CompanyHierarchyCacheImpl(final ComCompanyDao companyDao) {
		this(DEFAULT_TIMEOUT_MILLIS, DEFAULT_CAPACITY, companyDao);
	}
	
	public CompanyHierarchyCacheImpl(final int timeoutMillis, final int capacity, final ComCompanyDao companyDao) {
		this.companyToRootCompanyMap = new TimeoutLRUMap<>(capacity, timeoutMillis);
		this.companyDao = Objects.requireNonNull(companyDao, "company DAO");
	}
	
	@Override
	public int getRootCompanyId(final int companyId) {
		if( logger.isInfoEnabled()) {
			logger.info( "Determining root company for company ID " + companyId);
		}
		
		if( this.companyToRootCompanyMap.containsKey( companyId)) {
			int root = this.companyToRootCompanyMap.get( companyId);
			
			if( logger.isDebugEnabled()) {
				logger.debug( "Root company for company ID " + companyId + " is " + root);
			}
			
			return root;
		} else {
			int companyIdToCheck = companyId;
			
			Company company = this.companyDao.getCompany( companyIdToCheck);
			
			if( company == null)
				return 0;

			if( company.getParentCompanyId() == 0 || company.getParentCompanyId() == company.getId()) {
				if( logger.isDebugEnabled()) {
					logger.debug( "No parent company set, so company " + company.getId() + " is root company");
				}
				// No parent, so the company is its own root
				companyToRootCompanyMap.put( company.getId(), company.getId());
				
				return company.getId();
			} else {
				if( logger.isDebugEnabled()) {
					logger.debug( "Found parent company " + company.getParentCompanyId() + " for company " + company.getId() + " - checking that parent company");
				}

				int rootId = getRootCompanyId( company.getParentCompanyId());
				
				if( logger.isDebugEnabled()) {
					logger.debug( "Got root company " + rootId + " for parent company " + company.getParentCompanyId() + " of company " + company.getId());
				}
						
				companyToRootCompanyMap.put( company.getId(), rootId);
				
				return rootId;
			}
				
		}
	}

}
