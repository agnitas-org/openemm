/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.systemmessages.service.impl;

import static com.agnitas.messages.I18nString.hasMessageForKey;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.Company;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.password.PasswordReminderState;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.logon.web.CannotSendSecurityCodeException;
import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.service.SupervisorUtil;
import com.agnitas.emm.core.systemmessages.service.SystemMailMessageService;
import com.agnitas.messages.I18nString;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HtmlUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public class SystemMailMessageServiceImpl implements SystemMailMessageService {

    private static final String SECURITY_CODE_PLACEHOLDER = "${SECURITY_CODE}";
    private static final String USERNAME_PLACEHOLDER = "${USERNAME}";

    private static final Logger logger = LogManager.getLogger(SystemMailMessageServiceImpl.class);

    protected final ConfigService configService;
    private final AdminService adminService;
    private final CompanyService companyService;
    private final JavaMailService javaMailService;

    @Autowired
    public SystemMailMessageServiceImpl(ConfigService configService, AdminService adminService, @Lazy CompanyService companyService, JavaMailService javaMailService) {
        this.configService = configService;
        this.adminService = adminService;
        this.companyService = companyService;
        this.javaMailService = javaMailService;
    }

    @Override
    public void sendPasswordChangedMail(Admin admin) {
        int companyID = admin.getCompanyID();
        logger.info("Password for admin '{}' (admin ID {}, company ID {}) has been changed",
                admin.getUsername(), admin.getAdminID(), companyID);

        if (!configService.getBooleanValue(ConfigValue.SendPasswordChangedNotification, companyID)) {
            logger.info("Sending notifications on changed user password disabled for company ID {}", companyID);
            return;
        }

        try {
            MailSettings settings = getPasswordChangedMailSettings(admin);
            sendMail(settings, admin);
        } catch (Exception e) {
            logger.error("Unable to send notification mail on changed admin password", e);
        }
    }

    protected MailSettings getPasswordChangedMailSettings(Admin admin) {
        String subject = translateMsg("admin.passwordChanged.mail.subject", admin,
                admin.getUsername(), admin.getFirstName(), admin.getFullname());
        String textContent = translateMsg("admin.passwordChanged.mail.body.text", admin,
                admin.getUsername(), admin.getFirstName(), admin.getFullname());

        String htmlContent;
        if (hasMessageForKey("admin.passwordChanged.mail.body.html")) {
            htmlContent = translateMsg("admin.passwordChanged.mail.body.html", admin,
                    admin.getUsername(), admin.getFirstName(), admin.getFullname());
        } else {
            htmlContent = null;
        }

        return new MailSettings(subject, textContent, htmlContent);
    }

    protected String fillPasswordChangedContentPlaceholders(String content, Admin admin) {
        return content
                .replace("{0}", admin.getUsername())
                .replace("{1}", admin.getFirstName())
                .replace("{2}", admin.getFullname());
    }

    @Override
    public void sendEmailChangedMail(Admin admin, String oldEmail) {
        MailSettings settings = getSettingsForEmailChangedMail(admin);
        sendMail(settings, oldEmail, admin.getCompanyID());
    }

    protected MailSettings getSettingsForEmailChangedMail(Admin admin) {
        String text = translateMsg("user.adress.change.mail.body.html", admin, admin.getUsername(), admin.getEmail());

        return new MailSettings(
                translateMsg("user.adress.change.mail.subject", admin),
                text,
                HtmlUtils.replaceLineFeedsForHTML(text)
        );
    }

    protected String fillEmailChangedContentPlaceholders(String content, Admin admin) {
        return content
                .replace("{0}", admin.getFirstName())
                .replace("{1}", admin.getEmail());
    }

    @Override
    public void sendPasswordExpirationMail(Admin admin) {
        int daysLeft = getPasswordExpirationDaysLeft(admin);
        MailSettings settings = getPasswordExpirationMailSettings(admin, daysLeft);
        if (sendMail(settings, admin)) {
            adminService.setPasswordReminderState(admin.getAdminID(), PasswordReminderState.SENT);
        }
    }

    protected MailSettings getPasswordExpirationMailSettings(Admin admin, int daysLeft) {
        String text = translateMsg("password.reminder.email.content", admin, admin.getFirstName(), admin.getFullname(),
                admin.getUsername(), admin.getCompany().getShortname(), admin.getCompanyID(), daysLeft);

        return new MailSettings(
                translateMsg("password.reminder.email.subject", admin),
                text,
                HtmlUtils.replaceLineFeedsForHTML(text)
        );
    }

    protected String fillPasswordExpirationContentPlaceholders(String content, Admin admin, int daysLeft) {
        return content
                .replace("{0}", admin.getFirstName())
                .replace("{1}", admin.getFullname())
                .replace("{2}", admin.getUsername())
                .replace("{3}", admin.getCompany().getShortname())
                .replace("{4}", String.valueOf(admin.getCompanyID()))
                .replace("{5}", String.valueOf(daysLeft));
    }

    private int getPasswordExpirationDaysLeft(Admin admin) {
        int expirationDays = configService.getIntegerValue(ConfigValue.UserPasswordExpireDays, admin.getCompanyID());
        Date expirationDate = DateUtils.addDays(admin.getLastPasswordChange(), expirationDays);
        return DateUtilities.getDaysBetween(new Date(), expirationDate);
    }

    @Override
    public void sendPasswordResetMail(Admin admin, String passwordResetLink) {
        MailSettings settings = getPasswordResetMailSettings(admin, passwordResetLink);
        sendMail(settings, admin);
    }

    protected MailSettings getPasswordResetMailSettings(Admin admin, String passwordResetLink) {
        String subject = translateMsg("passwordReset.mail.subject", admin,
                admin.getUsername(), passwordResetLink, admin.getFirstName(), admin.getFullname());
        String textContent = translateMsg("passwordReset.mail.body.text", admin,
                passwordResetLink, admin.getUsername(), admin.getFirstName(), admin.getFullname());
        String htmlContent = translateMsg("passwordReset.mail.body.html", admin,
                passwordResetLink, admin.getUsername(), admin.getFirstName(), admin.getFullname());

        return new MailSettings(subject, textContent, htmlContent);
    }

    protected String fillPasswordResetContentPlaceholders(String content, Admin admin, String passwordResetLink) {
        return content
                .replace("{0}", admin.getUsername())
                .replace("{1}", passwordResetLink)
                .replace("{2}", admin.getFirstName())
                .replace("{3}", admin.getFullname());
    }

    @Override
    public void sendWelcomeMail(Admin admin, String passwordResetLink) {
        MailSettings settings = getWelcomeMailSettings(admin, passwordResetLink);
        sendMail(settings, admin);
    }

    protected MailSettings getWelcomeMailSettings(Admin admin, String passwordResetLink) {
        String subject = translateMsg("user.welcome.mail.subject", admin,
                admin.getUsername(), passwordResetLink, admin.getFirstName(), admin.getFullname());
        String textContent = translateMsg("user.welcome.mail.body.text", admin,
                admin.getUsername(), passwordResetLink, admin.getFirstName(), admin.getFullname());
        String htmlContent = translateMsg("user.welcome.mail.body.html", admin,
                admin.getUsername(), passwordResetLink, admin.getFirstName(), admin.getFullname());

        return new MailSettings(subject, textContent, htmlContent);
    }

    protected String fillWelcomeContentPlaceholders(String content, Admin admin, String passwordResetLink) {
        return content
                .replace("{0}", admin.getUsername())
                .replace("{1}", passwordResetLink)
                .replace("{2}", admin.getFirstName())
                .replace("{3}", admin.getFullname());
    }

    @Override
    public void sendSecurityCodeMail(Admin admin, String securityCode) throws CannotSendSecurityCodeException {
        MailSettings settings;
        String email;

        Supervisor supervisor = admin.getSupervisor();
        if (supervisor != null) {
            email = supervisor.getEmail();
            settings = getSupervisorSecurityCodeMailSettings(admin, securityCode);
        } else {
            email = admin.getEmail();
            settings = getSecurityCodeMailSettings(admin, securityCode);
        }

        try {
            if (!sendMail(settings, email, admin.getCompanyID())) {
                logger.error("Unable to send email with security code?");
                throw new CannotSendSecurityCodeException("Error sending mail with security code");
            }
        } catch (Exception e) {
            logger.error("Error sending email with security code", e);
            throw new CannotSendSecurityCodeException(email, e);
        }
    }

    protected MailSettings getSecurityCodeMailSettings(Admin admin, String securityCode) {
        String subject = translateSecurityCodeMsg("logon.hostauth.email.security_code.subject", securityCode, admin);
        String text = translateSecurityCodeMsg("logon.hostauth.email.security_code.content", securityCode, admin);

        return new MailSettings(subject, text, HtmlUtils.replaceLineFeedsForHTML(text));
    }

    protected MailSettings getSupervisorSecurityCodeMailSettings(Admin admin, String securityCode) {
        String subject = translateSecurityCodeMsg("logon.hostauth.email.security_code.subject_supervisor", securityCode, admin);
        String text = translateSecurityCodeMsg("logon.hostauth.email.security_code.content_supervisor", securityCode, admin);

        return new MailSettings(subject, text, HtmlUtils.replaceLineFeedsForHTML(text));
    }

    private String translateSecurityCodeMsg(String msgKey, String securityCode, Admin admin) {
        return translateMsg(msgKey, admin)
                .replace(USERNAME_PLACEHOLDER, admin.getUsername())
                .replace(SECURITY_CODE_PLACEHOLDER, securityCode)
                .replace("\\n", "\n");
    }

    protected String fillSecurityCodeContentPlaceholders(String content, Admin admin, String securityCode) {
        return content
                .replace("{0}", (admin.isSupervisor() ? SupervisorUtil.formatCompleteName(admin) : admin.getUsername()))
                .replace("{1}", securityCode)
                .replace("{2}", admin.getFirstName())
                .replace("{3}", admin.getFullname());
    }

    @Override
    public void sendDkimWarningMail(int companyId, String senderDomain) {
        Company company = companyService.getCompany(companyId);

        AgnUtils.splitAndTrimList(StringUtils.defaultString(company.getContactTech()))
                .stream()
                .filter(StringUtils::isNotBlank)
                .forEach(techEmail -> {
                    MailSettings settings = getDkimWarningMailSettings(techEmail, company, senderDomain);
                    sendMail(settings, techEmail, companyId);
                });
    }

    private MailSettings getDkimWarningMailSettings(String email, Company company, String senderDomain) {
        AdminEntry admin = adminService.findByEmail(email, company.getId());

        if (admin == null) {
            Locale locale = Optional.ofNullable(configService.getValue(ConfigValue.LocaleLanguage, company.getId()))
                    .map(Locale::new)
                    .orElse(Locale.UK);

            String salutationPart = translateMsg("email.dkimKey.missing.salutation.unknown", locale);
            return getDkimWarningMailSettings(salutationPart, company, senderDomain, locale);
        }

        return getDkimWarningMailSettings(admin, company, senderDomain);
    }

    protected MailSettings getDkimWarningMailSettings(AdminEntry admin, Company company, String senderDomain) {
        Locale locale = admin.getLocale();

        String salutationPart = translateMsg("email.dkimKey.missing.salutation", locale, admin.getFullname());
        return getDkimWarningMailSettings(salutationPart, company, senderDomain, locale);
    }

    private MailSettings getDkimWarningMailSettings(String salutationPart, Company company, String senderDomain, Locale locale) {
        String subject = translateMsg("email.dkimKey.missing.subject", locale, company.getShortname());
        String emailText = translateMsg("email.dkimKey.missing.text", locale, salutationPart, senderDomain);

        return new MailSettings(subject, emailText, HtmlUtils.replaceLineFeedsForHTML(emailText));
    }

    private String translateMsg(String key, Admin admin, Object... parameters) {
        return translateMsg(key, admin.getLocale(), parameters);
    }

    private String translateMsg(String key, Locale locale, Object... parameters) {
        return I18nString.getLocaleString(key, locale, parameters);
    }

    protected boolean sendMail(MailSettings settings, Admin admin) {
        return sendMail(settings, admin.getEmail(), admin.getCompanyID());
    }

    protected boolean sendMail(MailSettings settings, String email, int companyId) {
        return javaMailService.sendReplyEmail(
                companyId,
                email,
                settings.getSubject(),
                settings.getText(),
                settings.getHtml(),
                settings.getReplyEmail()
        );
    }

    protected static class MailSettings {

        private final String replyEmail;
        private final String subject;
        private final String text;
        private final String html;

        public MailSettings(String subject, String text, String html) {
            this(subject, text, html, "");
        }

        public MailSettings(String subject, String text, String html, String replyEmail) {
            this.subject = subject;
            this.text = text;
            this.html = html;
            this.replyEmail = replyEmail;
        }

        public String getSubject() {
            return subject;
        }

        public String getText() {
            return text;
        }

        public String getHtml() {
            return html;
        }

        public String getReplyEmail() {
            return replyEmail;
        }
    }
}
