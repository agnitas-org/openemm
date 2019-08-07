/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity.checks;

import org.agnitas.emm.core.velocity.checks.CompanyContextVelocityChecker;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.commons.hierarchy.CompanyHierarchyCache;

/**
 * Implementation of CompanyContextVelocityChecker that checks,
 * if the company ID used in the script and the company ID, in which the script is running,
 * have the same hierarchical root company.
 */
public class ComCompanyContextVelocityChecker extends CompanyContextVelocityChecker {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( ComCompanyContextVelocityChecker.class);

	@Override
	protected boolean isValidCompanyId(int scriptCompanyId, int companyContext) {
		int scriptRootCompany = this.hierarchyCache.getRootCompanyId( scriptCompanyId);
		int contextRootCompany = this.hierarchyCache.getRootCompanyId( companyContext);
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Root company for company " + scriptCompanyId + " in script is " + scriptRootCompany);
			logger.debug( "Root company for company " + companyContext + " (context company) is " + contextRootCompany);
		}
		
		if( logger.isInfoEnabled()) {
			logger.info( "Valid context: " + (scriptRootCompany == contextRootCompany));
		}
		
		return scriptRootCompany != 0 && contextRootCompany != 0 && scriptRootCompany == contextRootCompany;
	}
	
	// ------------------------------------------------------------ Dependency Injection
	/** Cache holding data about company hierarchy. */
	private CompanyHierarchyCache hierarchyCache;
	
	/**
	 * Set cache holding data about company hierarchy. 
	 * 
	 * @param hierarchyCache cache holding data about company hierarchy
	 */
	public void setCompanyHierarchyCache( CompanyHierarchyCache hierarchyCache) {
		this.hierarchyCache = hierarchyCache;
	}

}
