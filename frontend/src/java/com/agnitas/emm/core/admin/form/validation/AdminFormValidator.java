/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.form.validation;

import org.agnitas.util.AgnUtils;
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
	private static final String STAT_EMAIL_INPUT_NAME = "statEmail";
	private static final String PASSWORD_CONFIRM_INPUT_NAME = "passwordConfirm";

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
		if (StringUtils.length(username) < 3) {
			popups.field(USERNAME_INPUT_NAME, "error.username.tooShort");
			return false;
		}
		if(StringUtils.length(username) > 180) {
			popups.field(USERNAME_INPUT_NAME,"error.username.tooLong");
			return false;
		}
		return true;
	}

	private static boolean validateFullName(final AdminForm adminForm, final Popups popups) {
		final String fullname = adminForm.getFullname();
		if (StringUtils.length(fullname) < 2) {
			popups.field(FULLNAME_INPUT_NAME, "error.name.too.short");
			return false;
		}
		if (StringUtils.length(fullname) > 100) {
			popups.alert(FULLNAME_INPUT_NAME, "error.name.too.long");
			return false;
		}
		return true;
	}

	private static boolean validateFirstname(final AdminForm adminForm, final Popups popups) {
		final String firstName = adminForm.getFirstname();
		if (StringUtils.length(firstName) < 2) {
			popups.field(FIRSTNAME_INPUT_NAME, "error.name.too.short");
			return false;
		}
		if (StringUtils.length(firstName) > 100) {
			popups.field(FIRSTNAME_INPUT_NAME, "error.name.too.long");
			return false;
		}
		return true;
	}

	private static boolean validateCompanyName(final AdminForm adminForm, final Popups popups) {
		final String companyName = adminForm.getCompanyName();
		if(StringUtils.length(companyName) < 2) {
			popups.field(COMPANY_NAME_INPUT_NAME, "error.company.tooShort");
			return false;
		}
		if (StringUtils.length(companyName) > 100) {
			popups.field(COMPANY_NAME_INPUT_NAME, "error.company.tooLong");
			return false;
		}
		return true;
	}

	private static boolean validateEmail(final AdminForm adminForm, final Popups popups) {
		final String email = adminForm.getEmail();
		if (StringUtils.isEmpty(email)) {
			popups.field(EMAIL_INPUT_NAME, "error.email.empty");
			return false;
		}
		if (!AgnUtils.isEmailValid(email)) {
			popups.field(EMAIL_INPUT_NAME, "error.invalid.email");
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
			popups.field(STAT_EMAIL_INPUT_NAME, "error.invalid.email");
			return false;
		}
		return true;
	}

	private static boolean validatePasswordsMatch(final AdminForm adminForm, final Popups popups) {
		final String password = adminForm.getPassword(),
				passwordConfirm = adminForm.getPasswordConfirm();
		if (!StringUtils.equals(password, passwordConfirm)) {
			popups.field(PASSWORD_CONFIRM_INPUT_NAME, "error.password.mismatch");
			return false;
		}
		return true;
	}

	private static boolean validateGroups(final AdminForm adminForm, final Popups popups) {
		if(CollectionUtils.isEmpty(adminForm.getGroupIDs())) {
			popups.alert("error.user.group");
			return false;
		}
		return true;
	}
}
