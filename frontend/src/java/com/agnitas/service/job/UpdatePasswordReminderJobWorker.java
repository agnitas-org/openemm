/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import static com.agnitas.messages.I18nString.getLocaleString;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.service.JobWorker;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HtmlUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.password.PasswordReminderState;

public class UpdatePasswordReminderJobWorker extends JobWorker { // GWUA-5746

    private AdminService adminService;
    private JavaMailService mailService;
    private PreviewFactory previewFactory;

    private final Map<String, MailingContent> mailingsByLanguage = new HashMap<>(2);

    @Override
    public String runJob() throws Exception {
        adminService = serviceLookupFactory.getBeanAdminService();
        mailService = serviceLookupFactory.getBeanJavaMailService();
        previewFactory = serviceLookupFactory.getBeanPreviewFactory();

        adminService
                .getAdminsToWarnAboutPasswordExpiration(getCompaniesConstrains())
                .forEach(this::sendPasswordExpirationEmail);
        return null;
    }

    private void sendPasswordExpirationEmail(Admin admin) {
        String language = admin.getLocale().getLanguage();
        MailingContent mail = mailingsByLanguage.computeIfAbsent(language, l -> getMailingContent(admin));

        int companyId = admin.getCompanyID();
        if (mailService.sendEmail(companyId, admin.getEmail(), mail.getSubj(), mail.getText(), mail.getHtml())) {
            adminService.setPasswordReminderState(admin.getAdminID(), PasswordReminderState.SENT);
        }
    }

    private MailingContent getMailingContent(Admin admin) {
        int mailingId = getPasswordExpirationMailingId(admin);
        int daysLeft = getDaysLeft(admin);
        if (mailingId <= 0) {
            String text = getTextFromMessages(admin, daysLeft);
            return new MailingContent(
                    getLocaleString("GWUA.password.reminder.email.subject", admin.getLocale()),
                    text, HtmlUtils.replaceLineFeedsForHTML(text));
        }
        Page output = getMailingOutput(mailingId);
        return new MailingContent(
                output.getHeaderField("subject"),
                replacePlaceholders(output.getText(), admin, daysLeft),
                replacePlaceholders(output.getHTML(), admin, daysLeft)
        );
    }

    private static String replacePlaceholders(String content, Admin admin, int daysLeft) {
        return content
                .replace("{0}", admin.getFirstName())
                .replace("{1}", admin.getFullname())
                .replace("{2}", admin.getUsername())
                .replace("{3}", admin.getCompany().getShortname())
                .replace("{4}", String.valueOf(admin.getCompanyID()))
                .replace("{5}", String.valueOf(daysLeft));
    }

    private Page getMailingOutput(int mailingId) {
        Preview preview = previewFactory.createPreview();
        Page output = preview.makePreview(mailingId, 0, true);
        preview.done();
        return output;
    }

    private int getPasswordExpirationMailingId(Admin admin) {
        int mailingId = adminService.getPasswordExpirationMailingId(admin.getLocale().getLanguage());
        if (mailingId <= 0 && !"en".equalsIgnoreCase(admin.getLocale().getLanguage())) {
            mailingId = adminService.getPasswordExpirationMailingId("en");
        }
        return mailingId;
    }

    private String getTextFromMessages(Admin admin, int daysLeft) {
        return getLocaleString("GWUA.password.reminder.email.text", admin.getLocale(),
                admin.getFirstName(),
                admin.getFullname(),
                admin.getUsername(),
                admin.getCompany().getShortname(),
                admin.getCompanyID(),
                daysLeft);
    }

    private int getDaysLeft(Admin admin) {
        int expirationDays = configService.getIntegerValue(ConfigValue.UserPasswordExpireDays, admin.getCompanyID());
        Date expirationDate = DateUtils.addDays(admin.getLastPasswordChange(), expirationDays);
        return DateUtilities.getDaysBetween(new Date(), expirationDate);
    }
    
    private static class MailingContent {
        private final String subj;
        private final String text;
        private final String html;

        public MailingContent(String subject, String text, String html) {
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
