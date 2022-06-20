/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.globalblacklist.forms.validation;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.globalblacklist.forms.BlacklistForm;
import com.agnitas.web.mvc.Popups;

public class BlacklistFormValidator {

	private static final int REASON_MAX_LENGTH = 500;

	public boolean validate(final BlacklistForm form, final Popups popups) {
		boolean success = validateEmail(form, popups);
		success &= validateReason(form, popups);
		return success;
	}

	private boolean validateEmail(final BlacklistForm form, final Popups popups) {
		final String email = form.getEmail();
		
		if (StringUtils.isEmpty(email)) {
			popups.field("email", "error.email.empty");
			return false;
		}
		if(!BlacklistEmailPatternValidator.validateEmailPattern(email)) {
			popups.field("email", "error.invalid.email");
			return false;
		}
		
		return true;
	}

	private boolean validateReason(final BlacklistForm form, final Popups popups) {
		final String reason = form.getReason();
		if(StringUtils.length(reason) > REASON_MAX_LENGTH) {
			popups.field("reason", "error.reason.tooLong", REASON_MAX_LENGTH);
			return false;
		}
		return true;
	}
}
