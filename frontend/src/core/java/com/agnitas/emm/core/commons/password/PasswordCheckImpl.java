/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.password;

import java.util.Objects;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.password.policy.PasswordPolicy;
import com.agnitas.emm.core.commons.password.util.PasswordCheckUtil;
import com.agnitas.emm.core.commons.password.util.PasswordPolicyUtil;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;

public class PasswordCheckImpl implements PasswordCheck {

	private AdminService adminService;
	private ConfigService configService;

	@Override
	public boolean checkAdminPassword(String password, Admin admin, PasswordCheckHandler handler) {
		try {
            // Check password policy
			PasswordPolicyUtil.loadCompanyPasswordPolicy(admin.getCompanyID(), configService)
					.getPasswordPolicy()
					.checkPassword(password);

			// Check that given password differs from current Admin password
			if (adminService.isAdminPassword(admin, password)) {
				handler.handleMatchesCurrentPassword();

				return false;
			}

			return true;
		} catch (PolicyViolationException e) {
			PasswordCheckUtil.invokeHandler(e, handler);
			return false;
		}
	}

	@Override
	public boolean checkNewAdminPassword(String password, int companyID, PasswordCheckHandler handler) {
		try {
			// Check basic constraints
            PasswordPolicyUtil.loadCompanyPasswordPolicy(companyID, configService)
					.getPasswordPolicy()
					.checkPassword(password);

			return true;
		} catch (PolicyViolationException e) {
			PasswordCheckUtil.invokeHandler(e, handler);
			return false;
		}
	}

	@Override
	public SimpleServiceResult checkAdminPassword(String password, Admin admin) {
		try {
			// Check basic constraints
            PasswordPolicyUtil.loadCompanyPasswordPolicy(admin.getCompanyID(), configService)
					.getPasswordPolicy()
					.checkPassword(password);

			// Check that given password differs from current admin's password.
			if (adminService.isAdminPassword(admin, password)) {
				return new SimpleServiceResult(false, Message.of("error.password_must_differ"));
			}

			return new SimpleServiceResult(true);
		} catch (PolicyViolationException e) {
			return new SimpleServiceResult(false, PasswordCheckUtil.policyViolationsToMessages(e));
		}
	}

	@Override
	public SimpleServiceResult checkNewAdminPassword(String password, final PasswordPolicy passwordPolicy) {
		try {
			// Check basic constraints
			passwordPolicy.checkPassword(password);
			return new SimpleServiceResult(true);
		} catch (PolicyViolationException e) {
			return new SimpleServiceResult(false, PasswordCheckUtil.policyViolationsToMessages(e));
		}
	}
	
	protected final ConfigService getConfigService() {
		return this.configService;
	}

	public void setAdminService(AdminService service) {
		this.adminService = Objects.requireNonNull(service, "Admin service is null");
	}
	
	public void setConfigService(ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service is null");
	}
}
