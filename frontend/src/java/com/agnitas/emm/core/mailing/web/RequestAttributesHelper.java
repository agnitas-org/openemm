/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.web;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;

public final class RequestAttributesHelper {

	public static final String MAILNG_EDITABLE_REQUEST_ATTRIBUTE_NAME = "IS_MAILING_EDITABLE";
	
	private MailingPropertiesRules mailingPropertiesRules;
	
	public final void addMailingEditableAttribute(final Mailing mailing, final ComAdmin admin, final HttpServletRequest request) {
		request.setAttribute(MAILNG_EDITABLE_REQUEST_ATTRIBUTE_NAME, mailingPropertiesRules.isMailingContentEditable(mailing, admin));
	}
	
	@Required
	public final void setMailingPropertiesRules(final MailingPropertiesRules rules) {
		this.mailingPropertiesRules = Objects.requireNonNull(rules, "MailingPropertiesRules is null");
	}
}
