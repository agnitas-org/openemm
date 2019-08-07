/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity;

import org.agnitas.emm.core.commons.packages.PackageInclusionChecker;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.velocity.AgnVelocityUberspector;
import org.agnitas.emm.core.velocity.checks.CompanyContextVelocityChecker;

/**
 * Implementation of {@link AgnVelocityUberspector} checking 
 * OpenEMM and EMM classes.
 */
public class ComAgnVelocityUberspector extends AgnVelocityUberspector {
	
	/** Config service. */
	private final ConfigService configService;
	
	/**
	 * Instantiates a new Velocity uberspector.
	 *
	 * @param contextCompanyId ID of company executing the script
	 * @param checker checker for company ID
	 * @param configService config service
	 */
	public ComAgnVelocityUberspector(int contextCompanyId, CompanyContextVelocityChecker checker, ConfigService configService) {
		super( contextCompanyId, checker);
		
		this.configService = configService;
	}

	@Override
	protected PackageInclusionChecker createPackageInclusionChecker() {
		return new ComVelocityCheckPackageInclusionCheckerImpl();
	}
	
	@Override
	protected boolean isRuntimeCheckEnabled() {
		return configService.isVelocityRuntimeCheckEnabled( getContextCompanyId());
	}
	
	@Override
	protected boolean isAbortScriptsEnabled() {
		return configService.isVelocityScriptAbortEnabled( getContextCompanyId());
	}
}
