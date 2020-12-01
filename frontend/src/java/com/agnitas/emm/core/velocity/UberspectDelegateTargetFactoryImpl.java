/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.velocity.UberspectDelegateTargetFactory;
import org.agnitas.emm.core.velocity.checks.CompanyContextVelocityChecker;
import org.apache.velocity.util.introspection.Uberspect;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.JavaMailService;

/**
 * Implementation of {@link UberspectDelegateTargetFactory}.
 */
public class UberspectDelegateTargetFactoryImpl implements UberspectDelegateTargetFactory {
	/** Config service. */
	private ConfigService configService;
	
	private JavaMailService javaMailService;
	
	/**
	 * Runtime checker for velocity scripts checking company IDs used in script.
	 */
	private CompanyContextVelocityChecker companyContextChecker;
	
	@Override
	public Uberspect newDelegateTarget(int companyId) {
		return new AgnVelocityUberspector(companyId, companyContextChecker, configService, javaMailService);
	}

	/**
	 * Set checker for runtime checks of company IDs used in scripts.
	 * 
	 * @param checker
	 *            checker
	 */
	public void setCompanyContextVelocityChecker(CompanyContextVelocityChecker checker) {
		this.companyContextChecker = checker;
	}

	/**
	 * Set config service.
	 * 
	 * @param configService
	 *            config service
	 */
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}
}
