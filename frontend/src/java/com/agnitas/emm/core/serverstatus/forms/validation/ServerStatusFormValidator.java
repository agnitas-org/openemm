/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.forms.validation;

import com.agnitas.emm.core.serverstatus.forms.ServerStatusForm;
import com.agnitas.web.mvc.Popups;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

public class ServerStatusFormValidator {

	public boolean validateJobDescription(final ServerStatusForm form, final Popups popups) {
		final String jobDescription = form.getJobStart();
		if(StringUtils.isEmpty(jobDescription)) {
			popups.field("jobStart", "error.mailing.parameter.name");
			return false;
		}
		return true;
	}

	public boolean validateTestEmail(final ServerStatusForm form, final Popups popups) {
		final String testEmail = form.getSendTestEmail();
		if(StringUtils.isEmpty(testEmail)) {
			popups.field("sendTestEmail", "error.email.empty");
			return false;
		}
		if(!AgnUtils.isEmailValid(testEmail)) {
			popups.field("sendTestEmail", "error.email.invalid");
			return false;
		}
		return true;
	}

	public boolean validateDiagnosticEmail(final ServerStatusForm form, final Popups popups) {
		final String diagnosticEmail = form.getSendDiagnosis();
		if(StringUtils.isEmpty(diagnosticEmail)) {
			popups.field("sendDiagnosis", "error.email.empty");
			return false;
		}
		if(!AgnUtils.isEmailValid(diagnosticEmail)) {
			popups.field("sendDiagnosis", "error.email.invalid");
			return false;
		}
		return true;
	}
}
