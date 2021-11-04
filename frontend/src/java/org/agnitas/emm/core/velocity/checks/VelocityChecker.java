/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.checks;

import java.lang.reflect.Method;

import org.agnitas.emm.core.velocity.CheckType;

/**
 * Generic interface for runtime checks on Velocity scripts.
 */
@Deprecated // After completion of EMM-8360, this class can be removed without replacement
public interface VelocityChecker {
	
	/**
	 * Performs a check on a specific parameter of a method call.
	 * 
	 * @param method called method
	 * @param argument argument
	 * @param checkType type of check to be performed
	 * @param companyId company ID that is executing the script
	 * 
	 * @throws VelocityCheckerException on errors during check (violation of access privileges, ...)
	 */
	public void performCheck( Method method, Object argument, CheckType checkType, int companyId) throws VelocityCheckerException;
}
