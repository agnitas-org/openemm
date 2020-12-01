/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

import org.agnitas.emm.core.commons.password.policy.PasswordPolicy;
import org.agnitas.emm.core.commons.password.policy.WebservicePasswordPolicy;
import org.agnitas.emm.core.commons.password.util.PasswordCheckUtil;

import com.agnitas.beans.ComAdmin;
import com.agnitas.service.SimpleServiceResult;

/**
 * Implementation of {@link PasswordCheck}.
 */
public class WebservicePasswordCheckImpl implements PasswordCheck {
	
	/** Password policy for webservices. */
	private final PasswordPolicy policy = new WebservicePasswordPolicy();

	@Override
	public boolean checkAdminPassword(String password, ComAdmin admin, PasswordCheckHandler handler) {
		try {
			// Check basic constraints
			policy.checkPassword(password);

			return true;
		} catch(final PolicyViolationException e) {
			PasswordCheckUtil.invokeHandler(e, handler);

			return false;
		}
	}
	
	@Override
	public boolean checkNewAdminPassword(String password, int companyID, PasswordCheckHandler handler) {
		try {
			// Check basic constraints
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
			policy.checkPassword(password);

			return new SimpleServiceResult(true);
		} catch (final PolicyViolationException e) {
			return new SimpleServiceResult(false, PasswordCheckUtil.policyViolationsToMessages(e));
		}
	}

	@Override
	public SimpleServiceResult checkNewAdminPassword(String password, PasswordPolicy passwordPolicy) {
		try {
			policy.checkPassword(password);

			return new SimpleServiceResult(true);
		} catch (final PolicyViolationException e) {
			return new SimpleServiceResult(false, PasswordCheckUtil.policyViolationsToMessages(e));
		}
	}

}
