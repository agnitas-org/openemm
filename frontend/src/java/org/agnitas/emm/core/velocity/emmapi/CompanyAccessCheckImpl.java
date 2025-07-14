/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.emmapi;

import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.commons.hierarchy.CompanyHierarchyCache;

public final class CompanyAccessCheckImpl implements CompanyAccessCheck {

	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(CompanyAccessCheckImpl.class);
	
	/** Cache holding data about company hierarchy. */
	private CompanyHierarchyCache hierarchyCache;
	
	private JavaMailService javaMailService;
	private ConfigService configService;

	@Override
	public final void checkCompanyAccess(final int toCompanyId, final int byCompanyId) throws AccessToCompanyDeniedException {
		if(!isValidAccess(toCompanyId, byCompanyId)) {
			try {
				throw new AccessToCompanyDeniedException(toCompanyId, byCompanyId);
			} catch(final AccessToCompanyDeniedException e) {
				javaMailService.sendEmail(0, configService.getValue(ConfigValue.Mailaddress_Frontend), "Error in Velocity Script", "Error in Velocity Script: " + e.getMessage(), "Error in Velocity Script: " + e.getMessage());

				throw e;
			}
		}
	}
	
	private final boolean isValidAccess(final int toCompanyId, final int byCompanyId) {
		final int scriptRootCompany = this.hierarchyCache.getRootCompanyId(toCompanyId);
		final int contextRootCompany = this.hierarchyCache.getRootCompanyId(byCompanyId);
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Checking access to company %d by company %d running the script", toCompanyId, byCompanyId));
		}
		
		if( LOGGER.isInfoEnabled()) {
			LOGGER.info( "Valid context: " + (scriptRootCompany == contextRootCompany));
		}
		
		return scriptRootCompany != 0 && contextRootCompany != 0 && scriptRootCompany == contextRootCompany;
	}
	
	/**
	 * Set cache holding data about company hierarchy. 
	 * 
	 * @param hierarchyCache cache holding data about company hierarchy
	 */
	public final void setCompanyHierarchyCache(final CompanyHierarchyCache hierarchyCache) {
		this.hierarchyCache = Objects.requireNonNull(hierarchyCache, "CompanyHierarchyCache is null");
	}
	
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}
	
	public final void setJavaMailService(final JavaMailService service) {
		this.javaMailService = Objects.requireNonNull(service, "JavaMailService is null");
	}

}
