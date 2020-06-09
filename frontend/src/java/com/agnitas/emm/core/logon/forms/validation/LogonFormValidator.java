/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.forms.validation;

import com.agnitas.emm.core.logon.forms.LogonForm;
import com.agnitas.web.mvc.Popups;
import org.apache.commons.lang3.StringUtils;

public class LogonFormValidator {

	public boolean validate(final LogonForm form, final Popups popups) {
		boolean success = validateUsername(form, popups);
		success &= validatePassword(form, popups);
		return success;
	}

	private boolean validateUsername(final LogonForm form, final Popups popups) {
		final String username = form.getUsername();
		if(StringUtils.isBlank(username)) {
			popups.field("username", "error.username.required");
			return false;
		}
		return true;
	}

	private boolean validatePassword(final LogonForm form, final Popups popups) {
		final String password = form.getPassword();
		if(StringUtils.isBlank(password)) {
			popups.field("password", "error.password.required");
			return false;
		}
		return true;
	}
}
