/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service.impl;

import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminPasswordChangedNotifier;
import com.agnitas.messages.I18nString;

public final class MailAdminPasswordChangedNotifier implements AdminPasswordChangedNotifier {

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(MailAdminPasswordChangedNotifier.class);
	
	private final JavaMailService mailService;
	private final ConfigService configService;
	
	public MailAdminPasswordChangedNotifier(final JavaMailService mailService, final ConfigService configService) {
		this.mailService = Objects.requireNonNull(mailService, "Mail service is null");
		this.configService = Objects.requireNonNull(configService, "Config service is null");
	}
	
	@Override
	public final void notifyAdminAboutChangedPassword(final ComAdmin admin) {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Password for admin '%s' (admin ID %d, company ID %d) has been changed", admin.getUsername(), admin.getAdminID(), admin.getCompanyID()));
		}
		
		try {
			final String toAddress = admin.getEmail();
			final String replyToAddress = configService.getValue(ConfigValue.Mailaddress_Support);
			
			final String subject = I18nString.getLocaleString("admin.passwordChanged.mail.subject", admin.getLocale());
			final String bodyText = I18nString.getLocaleString("admin.passwordChanged.mail.body.text", admin.getLocale());
			final String bodyHtml = null;
			
			this.mailService.sendEmail(toAddress, null, replyToAddress, subject, bodyText, bodyHtml);
		} catch(final Exception e) {
			LOGGER.error("Unable to send notification mail on changed admin password", e);
		}
	}

}
