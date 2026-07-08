/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.password.util;

import com.agnitas.emm.core.commons.password.policy.PasswordPolicies;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;

/**
 * Utility class for handling password policies.
 */
public class PasswordPolicyUtil {

	private PasswordPolicyUtil() {

	}
	
	/**
	 * Load user password policy for given company.
	 * 
	 * @param companyID company ID
	 * @param configService configuration service
	 * 
	 * @return password policy
	 */
	public static PasswordPolicies loadCompanyPasswordPolicy(int companyID, ConfigService configService) {
		if (companyID == 0) {
			throw new IllegalArgumentException("Trying to load password policy for company ID 0");
		}
		
		final String policyName = configService.getValue(ConfigValue.PasswordPolicy, companyID);
		return PasswordPolicies.findByName(policyName);
	}
	
	/**
	 * Load supervisor password policy.
	 *  
	 * @param configService configuration service
	 * 
	 * @return supervisor password policy
	 */
	public static PasswordPolicies loadSupervisorPasswordPolicy(ConfigService configService) {
		final String policyName = configService.getValue(ConfigValue.SupervisorPasswordPolicy);
		return PasswordPolicies.findByName(policyName);
	}

}
