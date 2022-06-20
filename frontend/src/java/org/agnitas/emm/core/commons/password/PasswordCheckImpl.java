/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

import java.util.Objects;

import org.agnitas.emm.core.commons.password.policy.PasswordPolicies;
import org.agnitas.emm.core.commons.password.policy.PasswordPolicy;
import org.agnitas.emm.core.commons.password.util.PasswordCheckUtil;
import org.agnitas.emm.core.commons.password.util.PasswordPolicyUtil;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;

/**
 * Implementation of {@link PasswordCheck}.
 */
public class PasswordCheckImpl implements PasswordCheck {

	private AdminService adminService;
	private ConfigService configService;

	@Override
	public boolean checkAdminPassword(String password, ComAdmin admin, PasswordCheckHandler handler) {
		try {
			// Check basic constraints
			final PasswordPolicies policies = PasswordPolicyUtil.loadCompanyPasswordPolicy(admin.getCompanyID(), configService);
			assert policies != null; // By definition of loadCompanyPasswordPolicy()
			
			final PasswordPolicy policy = policies.getPasswordPolicy();
			assert policy != null; // by definition of PasswordPolicies
			
			// Check password policy
			policy.checkPassword(password);

			if (admin != null) {
				// Check that given password differs from current Admin password
				if (adminService.isAdminPassword(admin, password)) {
					handler.handleMatchesCurrentPassword();
					
					return false;
				}
			}

			return true;
		} catch(final PolicyViolationException e) {
			PasswordCheckUtil.invokeHandler(e, handler);

			return false;
		}
	}

	@Override
	public boolean checkNewAdminPassword(String password, final int companyID, PasswordCheckHandler handler) {
		try {
			// Check basic constraints
			final PasswordPolicies policies = PasswordPolicyUtil.loadCompanyPasswordPolicy(companyID, configService);
			assert policies != null; // By definition of loadCompanyPasswordPolicy()
			final PasswordPolicy policy = policies.getPasswordPolicy();
			assert policy != null; // by definition of PasswordPolicies
			policy.checkPassword(password);

			return true;
		} catch(final PolicyViolationException e) {
			PasswordCheckUtil.invokeHandler(e, handler);

			return false;
		}
	}

	@Override
	public SimpleServiceResult checkAdminPassword(String password, ComAdmin admin) {
		try {
			// Check basic constraints
			final PasswordPolicies policies = PasswordPolicyUtil.loadCompanyPasswordPolicy(admin.getCompanyID(), configService);
			assert policies != null; // By definition of loadCompanyPasswordPolicy()
			final PasswordPolicy policy = policies.getPasswordPolicy();
			assert policy != null; // by definition of PasswordPolicies
			policy.checkPassword(password);

			if (admin != null) {
				// Check that given password differs from current admin's password.
				if (adminService.isAdminPassword(admin, password)) {
					return new SimpleServiceResult(false, Message.of("error.password_must_differ"));
				}
			}

			return new SimpleServiceResult(true);
		} catch (final PolicyViolationException e) {
			return new SimpleServiceResult(false, PasswordCheckUtil.policyViolationsToMessages(e));
		}
	}

	@Override
	public SimpleServiceResult checkNewAdminPassword(String password, final PasswordPolicy passwordPolicy) {
		try {
			// Check basic constraints
			passwordPolicy.checkPassword(password);

			return new SimpleServiceResult(true);
		} catch (final PolicyViolationException e) {
			return new SimpleServiceResult(false, PasswordCheckUtil.policyViolationsToMessages(e));
		}
	}
	
	protected final ConfigService getConfigService() {
		return this.configService;
	}

	// ---------------------------------------------------------------------------------------------------- Dependency Injection
	@Required
	public final void setAdminService(final AdminService service) {
		this.adminService = Objects.requireNonNull(service, "Admin service is null");
	}
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service is null");
	}
}
