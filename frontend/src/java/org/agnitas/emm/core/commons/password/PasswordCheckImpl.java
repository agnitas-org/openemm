/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
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

	@Override
	public boolean checkAdminPassword(String password, ComAdmin admin, PasswordCheckHandler handler) {
		try {
			// Check basic constraints
			PasswordUtil.checkPasswordConstraints(password);

			if (admin != null) {
				// Check that given password differs from current Admin password
				if (adminService.isAdminPassword(admin, password)) {
					throw new PasswordMatchesCurrentPasswordException();
				}
			}

			return true;
		} catch(PasswordConstraintException e) {
			handleException(e, handler);

			return false;
		}
	}

	@Override
	public SimpleServiceResult checkAdminPassword(String password, ComAdmin admin) {
		try {
			PasswordUtil.checkPasswordConstraints(password);

			if (admin != null) {
				// Check that given password differs from current admin's password.
				if (adminService.isAdminPassword(admin, password)) {
					throw new PasswordMatchesCurrentPasswordException();
				}
			}

			return new SimpleServiceResult(true);
		} catch (PasswordConstraintException e) {
			return new SimpleServiceResult(false, asMessage(e));
		}
	}

	/**
	 * Resolve password exception and call corresponding method of handler.
	 *  
	 * @param ex password exception
	 * @param handler handler
	 */
	protected void handleException(PasswordConstraintException ex, PasswordCheckHandler handler) {
		try {
			throw ex;	// Just a trick to dispatch exception to handler methods. Required for subclassing...
		} catch(PasswordContainsNoLowerCaseLettersException e) {
			handler.handleNoLowerCaseLettersException();
		} catch(PasswordContainsNoUpperCaseLettersException e) {
			handler.handleNoUpperCaseLettersException();
		} catch(PasswordContainsNoDigitsException e) {
			handler.handleNoDigitsException();
		} catch(PasswordContainsNoSpecialCharactersException e) {
			handler.handleNoPunctuationException();
		} catch(PasswordTooShortException e) {
			handler.handlePasswordTooShort();
		} catch(PasswordMatchesCurrentPasswordException e) {
			handler.handleMatchesCurrentPassword();
		} catch(PasswordConstraintException e) {
			handler.handleGenericError();
		}
	}

	protected Message asMessage(PasswordConstraintException exception) {
		try {
			throw exception;
		} catch(PasswordContainsNoLowerCaseLettersException e) {
			return Message.of("error.password_no_lowercase_letters");
		} catch(PasswordContainsNoUpperCaseLettersException e) {
			return Message.of("error.password_no_uppercase_letters");
		} catch(PasswordContainsNoDigitsException e) {
			return Message.of("error.password_no_digits");
		} catch(PasswordContainsNoSpecialCharactersException e) {
			return Message.of("error.password_no_special_chars");
		} catch(PasswordTooShortException e) {
			return Message.of("error.password.tooShort");
		} catch(PasswordMatchesCurrentPasswordException e) {
			return Message.of("error.password_must_differ");
		} catch(PasswordConstraintException e) {
			return Message.of("error.password.general");
		}
	}

    @Override
    public boolean passwordChanged(String username, String password) {
    	final ComAdmin admin = adminService.getAdminByLogin(username, password);

    	if (StringUtils.isEmpty(password) || (admin != null && admin.getAdminID() > 0)) {
            return false;
        } else {
            return true;
        }
    }

	// ---------------------------------------------------------------------------------------------------- Dependency Injection
	@Required
	public void setAdminService(AdminService service) {
		this.adminService = Objects.requireNonNull(service, "Admin service is null");
	}
}
