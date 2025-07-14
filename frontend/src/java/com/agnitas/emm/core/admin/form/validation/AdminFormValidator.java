/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.form.validation;

import com.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.admin.form.AdminForm;
import com.agnitas.web.mvc.Popups;

public class AdminFormValidator {

	private static final String USERNAME_INPUT_NAME = "username";
	private static final String FULLNAME_INPUT_NAME = "fullname";
	private static final String FIRSTNAME_INPUT_NAME = "firstname";
	private static final String COMPANY_NAME_INPUT_NAME = "companyName";
	private static final String EMAIL_INPUT_NAME = "email";

	public static boolean validate(final AdminForm adminForm, final Popups popups) {
		boolean success;

		success = validateUsername(adminForm, popups);
		success &= validateFullName(adminForm, popups);
		success &= validateFirstname(adminForm, popups);
		success &= validateCompanyName(adminForm, popups);
		success &= validateEmail(adminForm, popups);
		success &= validateStatEmail(adminForm, popups);
		success &= validatePasswordsMatch(adminForm, popups);
		success &= validateGroups(adminForm, popups);

		return success;
	}

	private static boolean validateUsername(final AdminForm adminForm, final Popups popups) {
		final String username = adminForm.getUsername();
		if (StringUtils.trimToNull(username) == null) {
			popups.fieldError(USERNAME_INPUT_NAME, "error.username.blank");
			return false;
		} else if (StringUtils.trimToNull(username).length() < 3) {
			popups.fieldError(USERNAME_INPUT_NAME, "error.username.tooShort");
			return false;
		} else if(StringUtils.length(username) > 180) {
			popups.fieldError(USERNAME_INPUT_NAME,"error.username.tooLong");
			return false;
		}
		return true;
	}

	private static boolean validateFullName(final AdminForm adminForm, final Popups popups) {
		final String fullname = adminForm.getFullname();
		if (StringUtils.trimToNull(fullname) == null) {
			popups.fieldError(FULLNAME_INPUT_NAME, "error.fullname.blank");
			return false;
		} else if (StringUtils.length(fullname) > 100) {
			popups.fieldError(FULLNAME_INPUT_NAME, "error.fullname.tooLong");
			return false;
		}
		return true;
	}

	private static boolean validateFirstname(final AdminForm adminForm, final Popups popups) {
		final String firstname = adminForm.getFirstname();
		if (StringUtils.trimToNull(firstname) == null) {
			popups.fieldError(FIRSTNAME_INPUT_NAME, "error.firstname.blank");
			return false;
		} else if (StringUtils.length(firstname) > 100) {
			popups.fieldError(FIRSTNAME_INPUT_NAME, "error.firstname.tooLong");
			return false;
		}
		return true;
	}

	private static boolean validateCompanyName(final AdminForm adminForm, final Popups popups) {
		final String companyName = adminForm.getCompanyName();
		if (StringUtils.trimToNull(companyName) == null) {
			popups.fieldError(COMPANY_NAME_INPUT_NAME, "error.company.blank");
			return false;
		} else if(StringUtils.length(companyName) < 2) {
			popups.fieldError(COMPANY_NAME_INPUT_NAME, "error.company.tooShort");
			return false;
		} else if (StringUtils.length(companyName) > 100) {
			popups.fieldError(COMPANY_NAME_INPUT_NAME, "error.company.tooLong");
			return false;
		}
		return true;
	}

	private static boolean validateEmail(final AdminForm adminForm, final Popups popups) {
		final String email = adminForm.getEmail();
		if (StringUtils.isEmpty(email)) {
			popups.fieldError(EMAIL_INPUT_NAME, "error.email.empty");
			return false;
		}
		if (!AgnUtils.isEmailValid(email)) {
			popups.fieldError(EMAIL_INPUT_NAME, "error.invalid.email");
			return false;
		}
		return true;
	}

	private static boolean validateStatEmail(final AdminForm adminForm, final Popups popups) {
		final String statEmail = adminForm.getStatEmail();
		if (StringUtils.isEmpty(statEmail)) {
			return true;
		}
		if (!AgnUtils.isEmailValid(statEmail)) {
			popups.fieldError("statEmail", "error.invalid.email");
			return false;
		}
		return true;
	}

	private static boolean validatePasswordsMatch(final AdminForm adminForm, final Popups popups) {
        if (!StringUtils.equals(adminForm.getPassword(), adminForm.getPasswordConfirm())) {
			popups.fieldError("passwordConfirm", "error.password.mismatch");
			return false;
		}
		return true;
	}

	private static boolean validateGroups(final AdminForm adminForm, final Popups popups) {
		if (CollectionUtils.isEmpty(adminForm.getGroupIDs())) {
			popups.fieldError("groupIDs", "error.user.group");
			return false;
		}
		return true;
	}
}
