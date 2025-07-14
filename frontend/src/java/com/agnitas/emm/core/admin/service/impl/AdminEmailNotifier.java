/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service.impl;

import static com.agnitas.messages.I18nString.getLocaleString;

import java.util.Date;
import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.preview.Page;
import com.agnitas.preview.Preview;
import com.agnitas.preview.PreviewFactory;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HtmlUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminNotifier;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.password.PasswordReminderState;
import com.agnitas.messages.I18nString;

public final class AdminEmailNotifier implements AdminNotifier {

	private static final Logger LOGGER = LogManager.getLogger(AdminEmailNotifier.class);

	private final ConfigService configService;
	private final AdminService adminService;
	private final PreviewFactory previewFactory;
	private final JavaMailService javaMailService;

	public AdminEmailNotifier(final ConfigService configService, final AdminService adminService, final PreviewFactory previewFactory, final JavaMailService javaMailService) {
		this.configService = Objects.requireNonNull(configService, "Config service is null");
		this.adminService = Objects.requireNonNull(adminService, "AdminService service is null");
		this.previewFactory = Objects.requireNonNull(previewFactory, "PreviewFactory is null");
		this.javaMailService = Objects.requireNonNull(javaMailService, "JavaMailService is null");
	}

	@Override
	public void notifyAdminAboutChangedPassword(final Admin admin) {
        LOGGER.info(String.format("Password for admin '%s' (admin ID %d, company ID %d) has been changed", admin.getUsername(), admin.getAdminID(), admin.getCompanyID()));

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

				javaMailService.sendEmail(admin.getCompanyID(), admin.getEmail(), mailSubject, mailContentText, mailContentHtml);
			} catch (final Exception e) {
				LOGGER.error("Unable to send notification mail on changed admin password", e);
			}
		} else {
			LOGGER.info(String.format("Sending notifications on changed user password disabled for company ID %d", admin.getCompanyID()));
		}
	}

    @Override
    public void notifyAboutPasswordExpiration(Admin admin) {
        int companyId = admin.getCompanyID();
        EmailContent mail = getPasswordExpirationEmailContent(admin);
        if (javaMailService.sendEmail(companyId, admin.getEmail(), mail.getSubj(), mail.getText(), mail.getHtml())) {
            adminService.setPasswordReminderState(admin.getAdminID(), PasswordReminderState.SENT);
        }
    }

    private EmailContent getPasswordExpirationEmailContent(Admin admin) {
        int mailingId = getPasswordExpirationMailingId(admin);
        int daysLeft = getExpirationDaysLeft(admin);
        if (mailingId <= 0) {
            String text = getTextContentForPasswordExpirationMail(admin, daysLeft);
            return new EmailContent(
                    getLocaleString("password.reminder.email.subject", admin.getLocale()),
                    text, HtmlUtils.replaceLineFeedsForHTML(text));
        }
        Page output = getMailingOutput(mailingId);
        return new EmailContent(
                output.getHeaderField("subject"),
                fillPasswordExpirationContentWithAdminData(output.getText(), admin, daysLeft),
                fillPasswordExpirationContentWithAdminData(output.getHTML(), admin, daysLeft)
        );
    }

    private static String fillPasswordExpirationContentWithAdminData(String content, Admin admin, int daysLeft) {
        return content
                .replace("{0}", admin.getFirstName())
                .replace("{1}", admin.getFullname())
                .replace("{2}", admin.getUsername())
                .replace("{3}", admin.getCompany().getShortname())
                .replace("{4}", String.valueOf(admin.getCompanyID()))
                .replace("{5}", String.valueOf(daysLeft));
    }

    private int getPasswordExpirationMailingId(Admin admin) {
        int mailingId = adminService.getPasswordExpirationMailingId(admin.getLocale().getLanguage());
        if (mailingId <= 0 && !"en".equalsIgnoreCase(admin.getLocale().getLanguage())) {
            mailingId = adminService.getPasswordExpirationMailingId("en");
        }
        return mailingId;
    }

    private String getTextContentForPasswordExpirationMail(Admin admin, int daysLeft) {
        return getLocaleString("password.reminder.email.content", admin.getLocale(),
                admin.getFirstName(),
                admin.getFullname(),
                admin.getUsername(),
                admin.getCompany().getShortname(),
                admin.getCompanyID(),
                daysLeft);
    }

    private int getExpirationDaysLeft(Admin admin) {
        int expirationDays = configService.getIntegerValue(ConfigValue.UserPasswordExpireDays, admin.getCompanyID());
        Date expirationDate = DateUtils.addDays(admin.getLastPasswordChange(), expirationDays);
        return DateUtilities.getDaysBetween(new Date(), expirationDate);
    }

    @Override
    public void notifyAdminAboutChangedEmail(Admin admin, String oldEmail) {
        EmailContent email = getContentForEmailChangedMail(admin);
        javaMailService.sendEmail(admin.getCompanyID(), oldEmail, email.getSubj(), email.getText(), email.getHtml());
    }

    private EmailContent getContentForEmailChangedMail(Admin admin) {
        int mailingId = getEmailChangedMailingId(admin);
        if (mailingId <= 0) {
            String text = getTextForEmailChangedMail(admin);
            return new EmailContent(
                    getLocaleString("user.adress.change.mail.subject", admin.getLocale()),
                    text, HtmlUtils.replaceLineFeedsForHTML(text));
        }
        Page output = getMailingOutput(mailingId);
        return new EmailContent(
            output.getHeaderField("subject"),
            fillEmailChangedContentWithAdminData(output.getText(), admin),
            fillEmailChangedContentWithAdminData(output.getHTML(), admin));
    }

    private static String getTextForEmailChangedMail(Admin admin) {
        return getLocaleString("user.adress.change.mail.body.html",
                admin.getLocale(), admin.getUsername(), admin.getEmail());
    }

    private int getEmailChangedMailingId(Admin admin) {
        int mailingId = adminService.getEmailChangedMailingId(admin.getLocale().getLanguage());
        if (mailingId <= 0 && !"en".equalsIgnoreCase(admin.getLocale().getLanguage())) {
            mailingId = adminService.getEmailChangedMailingId("en");
        }
        return mailingId;
    }

    private static String fillEmailChangedContentWithAdminData(String content, Admin admin) {
        return content
                .replace("{0}", admin.getFirstName())
                .replace("{1}", admin.getEmail());
    }

    private Page getMailingOutput(int mailingId) {
        Preview preview = previewFactory.createPreview();
        Page output = preview.makePreview(mailingId, 0, true);
        preview.done();
        return output;
    }

    public static class EmailContent {

        private final String subj;
        private final String text;
        private final String html;

        public EmailContent(String subject, String text, String html) {
            this.subj = subject;
            this.text = text;
            this.html = html;
        }

        public String getSubj() {
            return subj;
        }

        public String getText() {
            return text;
        }

        public String getHtml() {
            return html;
        }
    }
}
