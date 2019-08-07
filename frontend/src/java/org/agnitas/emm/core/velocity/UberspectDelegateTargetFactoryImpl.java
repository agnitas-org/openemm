/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;

import org.agnitas.emm.core.velocity.checks.CompanyContextVelocityChecker;
import org.apache.velocity.util.introspection.Uberspect;

/**
 * Implementation of {@link UberspectDelegateTargetFactory}.
 */
public class UberspectDelegateTargetFactoryImpl implements UberspectDelegateTargetFactory {

	@Override
	public Uberspect newDelegateTarget(int companyId) {
		return new AgnVelocityUberspector( companyId, this.companyContextChecker);
	}

	// ------------------------------------------------------- Dependency Injection
	/** Checker for company context. */
	private CompanyContextVelocityChecker companyContextChecker;
	
	/**
	 * Set checker for company context.
	 * 
	 * @param checker checker for company context
	 */
	public void setCompanyContextVelocityChecker( CompanyContextVelocityChecker checker) {
		this.companyContextChecker = checker;
	}
}
