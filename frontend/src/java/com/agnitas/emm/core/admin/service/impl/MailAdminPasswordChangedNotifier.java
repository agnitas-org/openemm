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
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.apache.log4j.Logger;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminPasswordChangedNotifier;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.messages.I18nString;

public final class MailAdminPasswordChangedNotifier implements AdminPasswordChangedNotifier {

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(MailAdminPasswordChangedNotifier.class);

	private final ConfigService configService;
	private final AdminService adminService;
	private final PreviewFactory previewFactory;
	private final JavaMailService javaMailService;

	public MailAdminPasswordChangedNotifier(final ConfigService configService, final AdminService adminService, final PreviewFactory previewFactory, final JavaMailService javaMailService) {
		this.configService = Objects.requireNonNull(configService, "Config service is null");
		this.adminService = Objects.requireNonNull(adminService, "AdminService service is null");
		this.previewFactory = Objects.requireNonNull(previewFactory, "PreviewFactory is null");
		this.javaMailService = Objects.requireNonNull(javaMailService, "JavaMailService is null");
	}

	@Override
	public final void notifyAdminAboutChangedPassword(final ComAdmin admin) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Password for admin '%s' (admin ID %d, company ID %d) has been changed", admin.getUsername(), admin.getAdminID(), admin.getCompanyID()));
		}

		if (configService.getBooleanValue(ConfigValue.SendPasswordChangedNotification, admin.getCompanyID())) {
			try {
				final String mailSubject;
				final String mailContentHtml;
				final String mailContentText;
				int passwordChangedMailingId = adminService.getPasswordChangedMailingId(admin.getLocale().getLanguage());
				if (passwordChangedMailingId <= 0 && !"en".equalsIgnoreCase(admin.getLocale().getLanguage())) {
					passwordChangedMailingId = adminService.getPasswordChangedMailingId("en");
				}
				if (passwordChangedMailingId > 0) {
					final Preview preview = previewFactory.createPreview();
					final Page output = preview.makePreview(passwordChangedMailingId, 0, true);
					preview.done();

					mailSubject = output.getHeaderField("subject").replace("{0}", admin.getUsername()).replace("{1}", admin.getFirstName()).replace("{2}", admin.getFullname());
					mailContentText = output.getText().replace("{0}", admin.getUsername()).replace("{1}", admin.getFirstName()).replace("{2}", admin.getFullname());
					mailContentHtml = output.getHTML().replace("{0}", admin.getUsername()).replace("{1}", admin.getFirstName()).replace("{2}", admin.getFullname());
				} else {
					mailSubject = I18nString.getLocaleString("admin.passwordChanged.mail.subject", admin.getLocale(), admin.getUsername(), admin.getFirstName(), admin.getFullname());
					mailContentText = I18nString.getLocaleString("admin.passwordChanged.mail.body.text", admin.getLocale(), admin.getUsername(), admin.getFirstName(), admin.getFullname());
					if (I18nString.hasMessageForKey("admin.passwordChanged.mail.body.html")) {
						mailContentHtml = I18nString.getLocaleString("admin.passwordChanged.mail.body.html", admin.getLocale(), admin.getUsername(), admin.getFirstName(), admin.getFullname());
					} else {
						mailContentHtml = null;
					}
				}

				javaMailService.sendEmail(admin.getEmail(), mailSubject, mailContentText, mailContentHtml);
			} catch (final Exception e) {
				LOGGER.error("Unable to send notification mail on changed admin password", e);
			}
		} else {
			LOGGER.info(String.format("Sending notifications on changed user password disabled for company ID %d", admin.getCompanyID()));
		}
	}

}
