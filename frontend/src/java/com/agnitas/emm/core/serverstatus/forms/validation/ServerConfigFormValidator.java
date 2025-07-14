/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.forms.validation;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.serverstatus.forms.ServerConfigForm;
import com.agnitas.web.mvc.Popups;

public class ServerConfigFormValidator {

	private static final String INPUTS_PREFIX = "configForm.";

	private static final String NAME_INPUT_NAME = INPUTS_PREFIX + "name";
	private static final String VALUE_INPUT_NAME = INPUTS_PREFIX + "value";

	public boolean validate(final ServerConfigForm form, final Popups popups) {
		boolean success = validateName(form, popups);
		success &= validateValue(form, popups);
		return success;
	}

	private boolean validateName(final ServerConfigForm form, final Popups popups) {
		final String name = form.getName();
		if(StringUtils.isEmpty(name)) {
			popups.fieldError(NAME_INPUT_NAME, "error.name.is.empty");
			return false;
		}
		return true;
	}

	private boolean validateValue(final ServerConfigForm form, final Popups popups) {
		final String value = form.getValue();
		if(StringUtils.isEmpty(value)) {
			popups.fieldError(VALUE_INPUT_NAME, "error.mailing.parameter.value");
			return false;
		}
		return true;
	}

}
