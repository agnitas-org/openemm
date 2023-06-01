/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.form.validation;

import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.bounce.form.BounceFilterForm;
import com.agnitas.web.mvc.Popups;

public class BounceFilterFormValidator {

	public boolean validate(final BounceFilterForm form, final Popups popups) {
		boolean success = validateShortName(form, popups);
		success &= validateFilterEmail(form, popups);
		success &= validateForwardEmail(form, popups);
		return success;
	}

	private boolean validateShortName(final BounceFilterForm form, final Popups popups) {
		final String shortName = form.getShortName();
		if (StringUtils.trimToNull(shortName) == null) {
			popups.field("shortName", "error.name.is.empty");
			return false;
		} else if (StringUtils.trimToNull(shortName).length() < 3) {
			popups.field("shortName", "error.name.too.short");
			return false;
		}
		return true;
	}

	private boolean validateFilterEmail(final BounceFilterForm form, final Popups popups) {
		final String filterEmail = form.getFilterEmail();
		if (StringUtils.isEmpty(filterEmail)) {
			return true;
		}
		if (!AgnUtils.isValidBounceFilterAddress(filterEmail)) {
			popups.field("filterEmail", "error.invalidFilterEmail");
			return false;
		}
		return true;
	}

	private boolean validateForwardEmail(final BounceFilterForm form, final Popups popups) {
		final String forwardEmail = form.getForwardEmail();
		if (StringUtils.isEmpty(forwardEmail)) {
			return true;
		}
		if (!AgnUtils.isValidEmailAddresses(forwardEmail)) {
			popups.field("forwardEmail", "error.invalidForwardEmail");
			return false;
		}
		return true;
	}
}
