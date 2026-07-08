/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.forms.validation;

import com.agnitas.emm.core.serverstatus.forms.ServerConfigForm;
import com.agnitas.web.mvc.Popups;
import org.apache.commons.lang3.StringUtils;

public class ServerConfigFormValidator {

	private static final String INPUTS_PREFIX = "configForm.";

	private static final String NAME_INPUT_NAME = INPUTS_PREFIX + "name";
	private static final String VALUE_INPUT_NAME = INPUTS_PREFIX + "value";
	private static final String COMPANY_ID_INPUT_NAME = INPUTS_PREFIX + "value";

	public boolean validate(ServerConfigForm form, Popups popups) {
		boolean success = validateName(form, popups);
		success &= validateValue(form, popups);
		success &= validateCompanyId(form, popups);
		return success;
	}

	public boolean validateName(ServerConfigForm form, Popups popups) {
        if (StringUtils.isBlank(form.getName())) {
			popups.fieldError(NAME_INPUT_NAME, "error.name.is.empty");
			return false;
		}
		return true;
	}

	private boolean validateValue(ServerConfigForm form, Popups popups) {
        if (StringUtils.isBlank(form.getValue())) {
			popups.fieldError(VALUE_INPUT_NAME, "error.mailing.parameter.value");
			return false;
		}
		return true;
	}

	public boolean validateCompanyId(ServerConfigForm form, Popups popups) {
		if (form.getCompanyId() == null) {
			popups.fieldError(COMPANY_ID_INPUT_NAME, "error.default.required");
			return false;
		}

		if (form.getCompanyId() < 0) {
			popups.fieldError(COMPANY_ID_INPUT_NAME, "default.Invalid");
			return false;
		}

		return true;
	}

}
