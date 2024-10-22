/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.web;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.agnitas.messages.I18nString;

import org.agnitas.dao.LicenseType;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorageBundle;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.UserActivityLogActions;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminPreferences;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.logon.beans.LogonState;
import com.agnitas.emm.core.logon.beans.LogonStateBundle;
import com.agnitas.emm.core.logon.forms.LogonForm;
import com.agnitas.emm.core.logon.forms.LogonHostAuthenticationForm;
import com.agnitas.emm.core.logon.forms.LogonPasswordChangeForm;
import com.agnitas.emm.core.logon.forms.LogonResetPasswordForm;
import com.agnitas.emm.core.logon.forms.LogonTotpForm;
import com.agnitas.emm.core.logon.forms.validation.LogonFormValidator;
import com.agnitas.emm.core.logon.service.ClientHostIdService;
import com.agnitas.emm.core.logon.service.ComHostAuthenticationService;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.emm.core.logon.service.HostAuthenticationServiceException;
import com.agnitas.emm.core.logon.service.UnexpectedLogonStateException;
import com.agnitas.emm.core.sessionhijacking.web.SessionHijackingPreventionConstants;
import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.Anonymous;

import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LogonController implements XssCheckAware {
	/** The logger. */
    private static final Logger logger = LogManager.getLogger(LogonController.class);

    protected static final String PASSWORD_CHANGED_KEY = "com.agnitas.emm.core.logon.web.PASSWORD_CHANGED";
    // TODO: EMMGUI-714: replace with redesigned page
    public static final String PASSWORD_RESET_LINK_PATTERN = "/logon/reset-password.action?username={username}&token={token}";

    protected final ComLogonService logonService;
    protected final LoginTrackService loginTrackService;
    protected final ComHostAuthenticationService hostAuthenticationService;
    protected final WebStorage webStorage;
    protected final ConfigService configService;
    protected final UserActivityLogService userActivityLogService;
    protected final ClientHostIdService clientHostIdService;
    protected AdminService adminService;

    private final LogonFormValidator logonFormValidator = new LogonFormValidator();

    public LogonController(ComLogonService logonService, LoginTrackService loginTrackService, ComHostAuthenticationService hostAuthenticationService, WebStorage webStorage, ConfigService configService, UserActivityLogService userActivityLogService, final ClientHostIdService clientHostIdService, final AdminService adminService) {
        this.logonService = logonService;
        this.loginTrackService = loginTrackService;
        this.hostAuthenticationService = hostAuthenticationService;
        this.webStorage = webStorage;
        this.configService = configService;
        this.userActivityLogService = userActivityLogService;
        this.clientHostIdService = Objects.requireNonNull(clientHostIdService, "ClientHostIdService is null");
        this.adminService = Objects.requireNonNull(adminService, "AdminService is null");
    }

    // This exception should normally never occur unless user manually navigates to some endpoints.
    @ExceptionHandler(UnexpectedLogonStateException.class)
    public String onUnexpectedLogonStateException(UnexpectedLogonStateException e) {
        logger.debug("Unexpected logon state", e);

        return "redirect:/logonRedesigned.action";
    }

    @ExceptionHandler(HostAuthenticationServiceException.class)
    public String onHostAuthenticationServiceException(HostAuthenticationServiceException e, Popups popups) {
        logger.error("Host authentication exception", e);
        popups.alert("Error");

        return "redirect:/logonRedesigned.action";
    }

    @Anonymous
    @GetMapping("/logon.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String logonView(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonForm form, Model model, HttpServletRequest request, Popups popups, final HttpSession session) {
        return viewLogon(logonStateBundle, model, request, popups, session, false);
    }

    @Anonymous
    @GetMapping("/logonRedesigned.action")
    public String logonViewRedesigned(LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonForm form,
                                      @RequestParam(value = "afterLogout", required = false) boolean isAfterLogout, Model model,
                                      HttpServletRequest request, Popups popups, HttpSession session) {
        model.addAttribute("afterLogout", isAfterLogout);
        return viewLogon(logonStateBundle, model, request, popups, session, true);
    }

    private String viewLogon(LogonStateBundle logonStateBundle, Model model, HttpServletRequest request, Popups popups, HttpSession session, boolean redesigned) {
        if(session.getAttribute(SessionHijackingPreventionConstants.FORCED_LOGOUT_MARKER_ATTRIBUTE_NAME) != null) {
            popups.alert(new Message("logon.security.sessionCheck"));
            session.removeAttribute(SessionHijackingPreventionConstants.FORCED_LOGOUT_MARKER_ATTRIBUTE_NAME);
        }

        final SimpleServiceResult result = logonService.checkDatabase();

        if (result.isSuccess()) {
            logonStateBundle.toPendingState();

            return getLogonPage(model, request.getServerName(), request, redesigned);
        }

        popups.addPopups(result);
        model.addAttribute("supportEmergencyUrl", configService.getValue(ConfigValue.SupportEmergencyUrl));
        return redesigned ? "login_db_failure" : "logon_db_failure";
    }

    @Anonymous
    @PostMapping("/logon.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String logon(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonForm form, Model model, HttpServletRequest request, Popups popups) {
        return doLogon(logonStateBundle, form, model, request, popups, false);
    }

    @Anonymous
    @PostMapping("/logonRedesigned.action")
    public String logonRedesigend(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonForm form, Model model, HttpServletRequest request, Popups popups) {
        return doLogon(logonStateBundle, form, model, request, popups, true);
    }

    private String doLogon(LogonStateBundle logonStateBundle, LogonForm form, Model model, HttpServletRequest request, Popups popups, boolean redesigned) {
        if (!logonFormValidator.validate(form, popups)) {
            return getLogonPage(model, request.getServerName(), request, redesigned);
        }

        String clientIp = request.getRemoteAddr();
        ServiceResult<Admin> result = logonService.authenticate(form.getUsername(), form.getPassword(), clientIp);

        if (result.isSuccess()) {
            Admin admin = result.getResult();

            if (admin.isRestful()) {
                // Restfull webservice user may not logon via GUI
                form.setPassword(null);
                popups.addPopups(new ServiceResult<>(admin, false, Message.of("error.admin.gui.locked", admin.getUsername())));
                return getLogonPage(model, request.getServerName(), request, redesigned);
            }

            logonStateBundle.toTotpState(admin);

            String description = "log in IP: " + clientIp + " SessionID: " + request.getSession().getId();
            writeUserActivityLog(admin, new UserAction(UserActivityLogActions.LOGIN_LOGOUT.getLocalValue(), description));

            return redesigned
                    ? "redirect:/logon/totpRedesigned.action"
                    : "redirect:/logon/totp.action";
        }

        form.setPassword(null);

        popups.addPopups(result);
        return getLogonPage(model, request.getServerName(), request, redesigned);
    }
    
    @Anonymous
    @GetMapping("/logon/totp.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String totpShowForm(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonTotpForm form, final Model model, final HttpServletRequest request) {
    	return doTotpShowForm(logonStateBundle, form, logonStateBundle.getAdmin(), model, request, false);
    }

    @Anonymous
    @GetMapping("/logon/totpRedesigned.action")
    public String totpShowFormRedesigned(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonTotpForm form, final Model model, final HttpServletRequest request) {
        return doTotpShowForm(logonStateBundle, form, logonStateBundle.getAdmin(), model, request, true);
    }
    
    protected String doTotpShowForm(LogonStateBundle logonStateBundle, LogonTotpForm form, Admin admin, Model model, HttpServletRequest request, boolean redesigned) {
    	logonStateBundle.toAuthenticationState();
    	return redesigned ? "redirect:/logon/authenticate-hostRedesigned.action" : "redirect:/logon/authenticate-host.action";
    }
    
    @Anonymous
    @PostMapping("/logon/totp.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String totpVerifyValue(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonTotpForm form, final Popups popups, final HttpServletResponse response) { 
    	return doTotpVerifyValue(logonStateBundle, form, logonStateBundle.getAdmin(), popups, response, false);
    }

    @Anonymous
    @PostMapping("/logon/totpRedesigned.action")
    public String totpVerifyValueRedesigned(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonTotpForm form, final Popups popups, final HttpServletResponse response) {
        return doTotpVerifyValue(logonStateBundle, form, logonStateBundle.getAdmin(), popups, response, true);
    }
    
    protected String doTotpVerifyValue(LogonStateBundle logonStateBundle, LogonTotpForm form, Admin admin, Popups popups, HttpServletResponse response, boolean redesigned) {
    	logonStateBundle.toAuthenticationState();
    	return redesigned
                ? "redirect:/logon/authenticate-hostRedesigned.action"
                : "redirect:/logon/authenticate-host.action";
    }
    
    @Anonymous
    @GetMapping("/logonoffline.action")
    public String logonUrlOffline(@ModelAttribute("form") LogonForm form) {
        return "logon_offline";
    }

    @Anonymous
    @PostMapping("/logon/authenticate-host.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String hostAuthentication(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonHostAuthenticationForm form, RedirectAttributes redirectModel, Popups popups, final HttpServletResponse response) throws HostAuthenticationServiceException {
        return doHostAuthentication(logonStateBundle, form, redirectModel, popups, response, false);
    }

    @Anonymous
    @PostMapping("/logon/authenticate-hostRedesigned.action")
    public String hostAuthenticationRedesigned(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonHostAuthenticationForm form, RedirectAttributes redirectModel, Popups popups, final HttpServletResponse response) throws HostAuthenticationServiceException {
        return doHostAuthentication(logonStateBundle, form, redirectModel, popups, response, true);
    }

    private String doHostAuthentication(LogonStateBundle logonStateBundle, LogonHostAuthenticationForm form, RedirectAttributes redirectModel, Popups popups, HttpServletResponse response, boolean redesigned) throws HostAuthenticationServiceException {
        final Admin admin = logonStateBundle.getAdmin();

        logonStateBundle.requireLogonState(LogonState.HOST_AUTHENTICATION_SECURITY_CODE);

        final String authenticationCode = StringUtils.trim(form.getAuthenticationCode());
        final String hostId = logonStateBundle.getHostId();

        // Check if user submitted a valid authentication code.
        if (authenticateHost(admin, hostId, authenticationCode, popups)) {
            if(form.isTrustedDevice()) {
                this.clientHostIdService.createAndPublishHostAuthenticationCookie(hostId, admin.getCompanyID(), response);
            } else {
                logger.info("User does not trust device - 2FA cookie not set");
                this.hostAuthenticationService.removeAuthentictedHost(hostId);
            }

            logonStateBundle.toMaintainPasswordState();
            return redesigned ? "redirect:/logon/maintain-passwordRedesigned.action" : "redirect:/logon/maintain-password.action";
        } else {
            redirectModel.addFlashAttribute("form", form);
            return redesigned ? "redirect:/logon/authenticate-hostRedesigned.action" : "redirect:/logon/authenticate-host.action";
        }
    }

    @Anonymous
    @GetMapping("/logon/authenticate-host.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String hostAuthenticationAskSecurityCode(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonHostAuthenticationForm form, Model model, HttpServletRequest request, final Popups popups) throws HostAuthenticationServiceException {
        return authenticateHostView(logonStateBundle, model, request, popups, false);
    }

    @Anonymous
    @GetMapping("/logon/authenticate-hostRedesigned.action")
    public String hostAuthenticationAskSecurityCodeRedesigned(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonHostAuthenticationForm form, Model model, HttpServletRequest request, final Popups popups) throws HostAuthenticationServiceException {
        return authenticateHostView(logonStateBundle, model, request, popups, true);
    }

    private String authenticateHostView(LogonStateBundle logonStateBundle, Model model, HttpServletRequest request, Popups popups, boolean redesigned) throws HostAuthenticationServiceException {
        final Admin admin = logonStateBundle.getAdmin();

        // Simply skip this step if host authentication is not enabled.
        if (!hostAuthenticationService.isHostAuthenticationEnabled(admin.getCompanyID())) {
            // Host authentication is disabled for company of user. Skip this step.
            logger.info("Host authentication is DISABLED for company of user {}", admin.getUsername());

            logonStateBundle.toMaintainPasswordState();
            return redesigned ? "redirect:/logon/maintain-passwordRedesigned.action" : "redirect:/logon/maintain-password.action";
        }

        logger.info("Host authentication is ENABLED for company of user {}", admin.getUsername());
        final String hostId = this.clientHostIdService.getClientHostId(request).orElse(this.clientHostIdService.createHostId());

        // Check if a given hostId is marked as authenticated.
        if (authenticateHost(admin, hostId)) {
            logonStateBundle.toMaintainPasswordState();
            return redesigned ? "redirect:/logon/maintain-passwordRedesigned.action" : "redirect:/logon/maintain-password.action";
        }

        PasswordState state = logonService.getPasswordState(admin);
        if (state.equals(PasswordState.EXPIRED_LOCKED)) {
            return showPasswordExpiredPage(admin, model, popups, redesigned);
        }

        // The hostId is unknown so should be confirmed via email.
        logonStateBundle.toAuthenticateHostSecurityCodeState(hostId);

        String email = getEmailForHostAuthentication(admin);

        // Admin/supervisor must have an e-mail address where a security code is going to be sent.
        if (StringUtils.isBlank(email)) {
            popups.alert("logon.error.hostauth.no_address");
            return redesigned ? "redirect:/logonRedesigned.action" : "redirect:/logon.action";
        }

        if (hostId == null) {
            // If hostId is missing from cookies the cookies are probably disabled.
            popups.warning("logon.hostauth.cookies_disabled");
        }

        try {
            hostAuthenticationService.sendSecurityCode(admin, hostId);
        } catch (CannotSendSecurityCodeException e) {
            logger.error("Cannot send security code to {}", e.getReceiver());
            popups.alert("logon.error.hostauth.send_failed", email);
            return redesigned ? "redirect:/logonRedesigned.action" : "redirect:/logon.action";
        } catch (Exception e) {
            logger.error("Error generating or sending security code", e);
            popups.alert("logon.error.hostauth.send_failed", email);
            return redesigned ? "redirect:/logonRedesigned.action" : "redirect:/logon.action";
        }

        model.addAttribute("adminMailAddress", getEmailForHostAuthentication(logonStateBundle.getAdmin()));
        model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));
        return redesigned ? "login_host_authentication" : "logon_host_authentication";
    }

    @Anonymous
    @GetMapping("/logon/maintain-password.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String maintainPassword(final LogonStateBundle logonStateBundle, Model model, Popups popups) {
    	return viewMaintainPassword(logonStateBundle, model, popups, false);
    }

    @Anonymous
    @GetMapping("/logon/maintain-passwordRedesigned.action")
    public String maintainPasswordRedesigned(final LogonStateBundle logonStateBundle, Model model, Popups popups) {
        return viewMaintainPassword(logonStateBundle, model, popups, true);
    }

    private String viewMaintainPassword(LogonStateBundle logonStateBundle, Model model, Popups popups, boolean redesigned) {
        logonStateBundle.requireLogonState(LogonState.MAINTAIN_PASSWORD);

        PasswordState state = logonService.getPasswordState(logonStateBundle.getAdmin());

        if (state != PasswordState.VALID) {
            if (state.equals(PasswordState.EXPIRED_LOCKED)) {
                return showPasswordExpiredPage(logonStateBundle.getAdmin(), model, popups, redesigned);
            }

            logonStateBundle.toPasswordChangeState();
            return redesigned ? "redirect:/logon/change-passwordRedesigned.action" : "redirect:/logon/change-password.action";
        }

        logonStateBundle.toCompleteState();
        return redesigned ? "forward:/startRedesigned.action" : "forward:/start.action";
    }

    @Anonymous
    @PostMapping("/logon/change-password.action")
    public String changePassword(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonPasswordChangeForm form, RedirectAttributes model, Popups popups) {
    	return doChangePassword(logonStateBundle, form, model, popups, false);
    }

    @Anonymous
    @PostMapping("/logon/change-passwordRedesigned.action")
    public String changePasswordRedesigned(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonPasswordChangeForm form, RedirectAttributes model, Popups popups) {
        return doChangePassword(logonStateBundle, form, model, popups, true);
    }

    private String doChangePassword(LogonStateBundle logonStateBundle, LogonPasswordChangeForm form, RedirectAttributes model, Popups popups, boolean redesigned) {
        logonStateBundle.requireLogonState(LogonState.CHANGE_ADMIN_PASSWORD, LogonState.CHANGE_SUPERVISOR_PASSWORD);

        Admin admin = logonStateBundle.getAdmin();

        if (form.isSkip()) {
            PasswordState state = logonService.getPasswordState(admin);

            // In some cases a password change cannot be skipped.
            if (state == PasswordState.VALID || state == PasswordState.EXPIRING) {
                logonStateBundle.toCompleteState();
                return redesigned ? "forward:/startRedesigned.action" : "forward:/start.action";
            }
        } else {
            SimpleServiceResult result = logonService.setPassword(admin, form.getPassword());

            if (result.isSuccess()) {
                logonStateBundle.toCompleteState();
                // Show success page which indicates that password has been changed.
                model.addFlashAttribute(PASSWORD_CHANGED_KEY, true);
            } else {
                popups.addPopups(result);
                // Show password change page and the error message(s).
                model.addFlashAttribute("isAnotherAttempt", true);
            }
        }

        return redesigned ? "redirect:/logon/change-passwordRedesigned.action" : "redirect:/logon/change-password.action";
    }

    @Anonymous
    @GetMapping("/logon/change-password.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String changePassword(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonPasswordChangeForm form, Model model, HttpServletRequest request, Popups popups) {
        return viewChangePassword(logonStateBundle, model, request, popups, false);
    }

    @Anonymous
    @GetMapping("/logon/change-passwordRedesigned.action")
    public String changePasswordRedesigned(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonPasswordChangeForm form, Model model, Popups popups, HttpServletRequest request) {
        return viewChangePassword(logonStateBundle, model, request, popups, true);
    }

    private String viewChangePassword(LogonStateBundle logonStateBundle, Model model, HttpServletRequest request, Popups popups, boolean redesigned) {
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));

        if (model.containsAttribute(PASSWORD_CHANGED_KEY)) {
            logonStateBundle.requireLogonState(LogonState.COMPLETE);
            if (redesigned) {
                popups.success("password.changed.proceed", request.getContextPath() + "/logonRedesigned.action");
            }
            return redesigned ? "login_password_changed" : "logon_password_changed";
        } else {
            logonStateBundle.requireLogonState(LogonState.CHANGE_ADMIN_PASSWORD, LogonState.CHANGE_SUPERVISOR_PASSWORD);

            Admin admin = logonStateBundle.getAdmin();
            PasswordState state = logonService.getPasswordState(admin);

            // Expiration date is only required if password is already expired or a "deadline" is coming.
            if (state == PasswordState.EXPIRING || state == PasswordState.EXPIRED) {
                Date expirationDate = logonService.getPasswordExpirationDate(admin);

                if (expirationDate != null) {
                    model.addAttribute("expirationDate", admin.getDateFormat().format(expirationDate));
                }
            }

            model.addAttribute("helplanguage", logonService.getHelpLanguage(admin));
            model.addAttribute("isSupervisor", admin.isSupervisor());
            model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));
            model.addAttribute("isExpiring", state == PasswordState.EXPIRING);
            model.addAttribute("isExpired", state == PasswordState.EXPIRED || state == PasswordState.ONE_TIME);

            return redesigned ? "login_password_change" : "logon_password_change";
        }
    }

    private String showPasswordExpiredPage(Admin admin, Model model, Popups popups, boolean redesigned) {
        Date expirationDate = logonService.getPasswordExpirationDate(admin);

        if (!redesigned) {
            if (expirationDate != null) {
                model.addAttribute("expirationDate", admin.getDateFormat().format(expirationDate));
            }

            model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));
            model.addAttribute("helplanguage", logonService.getHelpLanguage(admin));
            model.addAttribute("isSupervisor", admin.isSupervisor());
        } else {
            String errorMessage = I18nString.getLocaleString("password.change.notification.expired", admin.getLocale(), admin.getDateFormat().format(expirationDate));
            if (!admin.isSupervisor()) {
                errorMessage += "<br/>" + I18nString.getLocaleString("password.finally.expired.info", admin.getLocale());
            }

            popups.exactAlert(errorMessage);
        }

        return redesigned ? "login_password_expired_locked" : "logon_password_expired_locked";
    }

    @Anonymous
    @PostMapping("/start.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String start(final LogonStateBundle logonStateBundle, Admin admin, @RequestParam(required = false) String webStorageJson, Model model, Popups popups, final HttpServletRequest request) {
        return doStart(logonStateBundle, admin, webStorageJson, model, popups, request, false);
    }

    @Anonymous
    @PostMapping("/startRedesigned.action")
    public String startRedesigned(final LogonStateBundle logonStateBundle, Admin admin, @RequestParam(required = false) String webStorageJson, Model model, Popups popups, final HttpServletRequest request) {
        return doStart(logonStateBundle, admin, webStorageJson, model, popups, request, true);
    }

    private String doStart(LogonStateBundle logonStateBundle, Admin admin, String webStorageJson, Model model, Popups popups, HttpServletRequest request, boolean redesigned) {
        if (admin == null) {
            logonStateBundle.requireLogonState(LogonState.COMPLETE);

            if (webStorageJson == null) {
                return getLogonCompletePage(logonStateBundle.getAdmin(), model, redesigned);
            }

            // Finalize logon procedure, drop temporary data, setup session attributes.
            return complete(logonStateBundle.toCompleteState(request), webStorageJson, popups);
        }

        // Redirect to EMM start page.
        return "redirect:/dashboard.action";
    }

    @Anonymous
    @GetMapping("/start.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String startView(final LogonStateBundle logonStateBundle, Admin admin, Model model) {
        return viewStart(logonStateBundle, admin, model, false);
    }

    @Anonymous
    @GetMapping("/startRedesigned.action")
    public String startViewRedesigned(final LogonStateBundle logonStateBundle, Admin admin, Model model) {
        return viewStart(logonStateBundle, admin, model, true);
    }

    private String viewStart(LogonStateBundle logonStateBundle, Admin admin, Model model, boolean redesigned) {
        if (admin == null) {
            // No need to check login state here. This GET requests just displays the login form.
            return getLogonCompletePage(logonStateBundle.getAdmin(), model, redesigned);
        }

        // Admin is already in, redirect to EMM start page.
        return "redirect:/dashboard.action";
    }

    @Anonymous
    @PostMapping("/logon/reset-password.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String resetPassword(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonResetPasswordForm form, RedirectAttributes model, Popups popups, HttpServletRequest request) throws HostAuthenticationServiceException {
        return doResetPassword(logonStateBundle, form, model, popups, request, false);
    }

    @Anonymous
    @PostMapping("/logon/reset-passwordRedesigned.action")
    public String resetPasswordRedesigned(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonResetPasswordForm form, RedirectAttributes model, Popups popups, HttpServletRequest request) throws HostAuthenticationServiceException {
        return doResetPassword(logonStateBundle, form, model, popups, request, true);
    }

    private String doResetPassword(LogonStateBundle logonStateBundle, LogonResetPasswordForm form, RedirectAttributes model, Popups popups, HttpServletRequest request, boolean redesigned) throws HostAuthenticationServiceException {
        String clientIp = request.getRemoteAddr();

        if (StringUtils.isNotEmpty(form.getUsername()) && StringUtils.isNotEmpty(form.getToken())) {
            ServiceResult<Admin> result = logonService.resetPassword(form.getUsername(), form.getToken(), form.getPassword(), clientIp);

            if (result.isSuccess()) {
                Admin admin = result.getResult();

                // Mark this host as authenticated (if authentication is enabled).
                if (hostAuthenticationService.isHostAuthenticationEnabled(admin.getCompanyID())) {
                    // Take hostId from cookies or session (if any), generate a new one otherwise.
                    final String hostId = this.clientHostIdService.getClientHostId(request).orElse(this.clientHostIdService.createHostId());

                    hostAuthenticationService.writeHostAuthentication(admin, hostId);
                }

                logonStateBundle.toCompleteState(admin);

                writeUserActivityLog(admin, "change password", admin.getUsername() + " (" + admin.getAdminID() + ")");
                writeUserActivityLog(admin, UserActivityLogActions.LOGIN_LOGOUT.getLocalValue(), "logged in after password reset via " + admin.getEmail() + " from " + AgnUtils.getIpAddressForStorage(request));

                return redesigned ? "forward:/logonRedesigned.action" : "forward:/logon.action";
            }

            popups.addPopups(result);
        } else {
            SimpleServiceResult result = logonService.requestPasswordReset(form.getUsername(), form.getEmail(), clientIp, PASSWORD_RESET_LINK_PATTERN);
            popups.addPopups(result);
        }

        model.addFlashAttribute("form", form);

        return redesigned ? "redirect:/logon/reset-passwordRedesigned.action" : "redirect:/logon/reset-password.action";
    }
    
    @Anonymous
    @GetMapping("/logon/reset-password.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String resetPasswordView(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonResetPasswordForm form, Model model, Popups popups, HttpServletRequest request) {
        return viewResetPassword(logonStateBundle, form, model, popups, request, false);
    }

    @Anonymous
    @GetMapping("/logon/reset-passwordRedesigned.action")
    public String resetPasswordViewRedesigned(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonResetPasswordForm form, Model model, Popups popups, HttpServletRequest request) {
        return viewResetPassword(logonStateBundle, form, model, popups, request, true);
    }

    private String viewResetPassword(LogonStateBundle logonStateBundle, LogonResetPasswordForm form, Model model, Popups popups, HttpServletRequest request, boolean redesigned) {
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));
        model.addAttribute("helplanguage", logonService.getHelpLanguage(null));

        if (StringUtils.isNotEmpty(form.getUsername()) && StringUtils.isNotEmpty(form.getToken())) {
            if (!logonService.existsPasswordResetTokenHash(form.getUsername(), form.getToken())) {
                logonService.riseErrorCount(form.getUsername());
                ServiceResult<Admin> result = new ServiceResult<>(null, false, Message.of("error.passwordReset.auth"));
                popups.addPopups(result);
                return redesigned ? "redirect:/logon/reset-passwordRedesigned.action" : "redirect:/logon/reset-password.action";
            }

            if (!logonService.isValidPasswordResetTokenHash(form.getUsername(), form.getToken())) {
                ServiceResult<Admin> result = new ServiceResult<>(null, false, Message.of("error.passwordReset.expired", ComLogonService.TOKEN_EXPIRATION_MINUTES, configService.getValue(ConfigValue.SystemUrl) + "/logon/reset-password.action"));
                popups.addPopups(result);
                return redesigned ? "redirect:/logon/reset-passwordRedesigned.action" : "redirect:/logon/reset-password.action";
            }

            return redesigned ? "login_password_reset" : "logon_password_reset";
        } else {
            Admin admin = logonStateBundle.getAdmin();

            if (admin != null && StringUtils.isEmpty(form.getUsername()) && StringUtils.isEmpty(form.getEmail())) {
                form.setUsername(admin.getUsername());
                form.setEmail(admin.getEmail());
            }

            return redesigned
                    ? "login_password_reset_request"
                    : "logon_password_reset_request";
        }
    }

    @Anonymous
    @PostMapping("/logout.action")
    public String logout(Admin admin, HttpSession session) {
        // Invalidate existing session and create a new one (in order to clear stored attributes).
        session.invalidate();

        if (admin != null) {
            logger.info("User {} logged off", admin.getUsername());

            writeUserActivityLog(admin, new UserAction(UserActivityLogActions.LOGIN_LOGOUT.getLocalValue(), "log out"));

            if (admin.isRedesignedUiUsed()) {
                return "redirect:/logonRedesigned.action?afterLogout=true";
            }
        }

        return "redirect:/logout.action";
    }

    @Anonymous
    @GetMapping("/logout.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String logoutView(Admin admin, Model model, HttpServletRequest request) {
        if (admin == null) {
            model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));
            model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));
            return "logged_out";
        }

        return "logout";
    }

    private String getEmailForHostAuthentication(Admin admin) {
        Supervisor supervisor = admin.getSupervisor();

        if (supervisor == null) {
            return admin.getEmail();
        }

        return supervisor.getEmail();
    }

    private boolean authenticateHost(Admin admin, String hostId) throws HostAuthenticationServiceException {
        if (StringUtils.isEmpty(hostId)) {
            logger.info("Missing host ID cookie - host is assumed to be not authenticated!");
            return false;
        }

        if (hostAuthenticationService.isHostAuthenticated(admin, hostId)) {
            // Host is authenticated for user, so we can proceed to next login step
            logger.info("Host is already authenticated for user {}", admin.getUsername());
            hostAuthenticationService.writeHostAuthentication(admin, hostId);
            return true;
        } else {
            logger.info("Host is not authenticated for user {}", admin.getUsername());
            return false;
        }
    }

    private boolean authenticateHost(Admin admin, String hostId, String authenticationCode, Popups popups) throws HostAuthenticationServiceException {
        Supervisor supervisor = admin.getSupervisor();
        String expectedCode = hostAuthenticationService.getPendingSecurityCode(admin, hostId);

        if (StringUtils.isBlank(authenticationCode)) {
            popups.fieldError("authenticationCode", "logon.error.hostauth.empty_code");
            return false;
        }

        // Check authentication code
        if (authenticationCode.equals(expectedCode)) {
            if (supervisor == null) {
                logger.info("Authentication code correct for admin {} on host {}", admin.getAdminID(), hostId);
            } else {
                logger.info("Authentication code correct for supervisor {} on host {}", supervisor.getId(), hostId);
            }

            hostAuthenticationService.writeHostAuthentication(admin, hostId);

            return true;
        } else {
            popups.fieldError("authenticationCode", "logon.error.hostauth.invalid_code");

            if (supervisor == null) {
                logger.info("Invalid authentication code for admin {} on host {}", admin.getAdminID(), hostId);
            } else {
                logger.info("Invalid authentication code for supervisor {} on host {}", supervisor.getId(), hostId);
            }

            return false;
        }
    }

    private String complete(final Admin admin, final String webStorageJson, final Popups popups) {
        final AdminPreferences preferences = logonService.getPreferences(admin);
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        attributes.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN, admin, RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES, preferences, RequestAttributes.SCOPE_SESSION);
        logonService.updateSessionsLanguagesAttributes(admin);
        attributes.setAttribute("emmLayoutBase", logonService.getEmmLayoutBase(admin), RequestAttributes.SCOPE_SESSION);

        attributes.setAttribute("userName", StringUtils.defaultString(admin.getUsername()), RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute("firstName", StringUtils.defaultString(admin.getFirstName()), RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute("fullName", admin.getFullname(), RequestAttributes.SCOPE_SESSION);
        if (admin.getSupervisor() != null) {
        	attributes.setAttribute("supervisorName", admin.getSupervisor().getFullName(), RequestAttributes.SCOPE_SESSION);
            attributes.setAttribute("supervisorUsername", admin.getSupervisor().getSupervisorName(), RequestAttributes.SCOPE_SESSION);
        }
        attributes.setAttribute("companyShortName", admin.getCompany().getShortname(), RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute("companyID", admin.getCompany().getId(), RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute("adminTimezone", admin.getAdminTimezone(), RequestAttributes.SCOPE_SESSION);

        LicenseType licenseType = LicenseType.getLicenseTypeByID(configService.getValue(ConfigValue.System_License_Type));
		if (!configService.getBooleanValue(ConfigValue.IsLiveInstance)
        		|| licenseType == LicenseType.Inhouse
                || licenseType == LicenseType.OpenEMM
                || licenseType == LicenseType.OpenEMM_Plus
        		|| configService.getIntegerValue(ConfigValue.System_Licence) == 0) {
            attributes.setAttribute("versionNumber", configService.getValue(ConfigValue.ApplicationVersion), RequestAttributes.SCOPE_SESSION);
        }

        // Setup web-storage using client's data represented as JSON.
        webStorage.setup(webStorageJson);

        // Skip last successful login, because that's the current login.
        final int times = loginTrackService.countFailedLoginsSinceLastSuccess(admin.getUsername(), true);
        if (times > 0) {
            if (times > 1) {
                popups.alert("warning.failed_logins.more", times);
            } else {
                popups.alert("warning.failed_logins.1", times);
            }
        }
        AgnUtils.updateBrowserCacheMarker(); // update for admin related data in config.js
        return "redirect:/dashboard.action";
    }

    private String getLogonPage(Model model, String serverName, final HttpServletRequest request, boolean redesigned) {
        model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));
        model.addAttribute("iframeUrl", getLogonIframeUrl());
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(serverName));
        
        model.addAttribute("SHOW_TAB_HINT", showTabHint(request));
        
        return redesigned ? "login" : "logon";
    }
    
    protected boolean showTabHint(final HttpServletRequest request) {
    	return !request.getServletContext().getEffectiveSessionTrackingModes().contains(SessionTrackingMode.COOKIE);
    }
    
    protected String getLogonIframeUrl() {
		try {
			String connectionUrl;
			//Check locale
			if (AgnUtils.isGerman(LocaleContextHolder.getLocale())) {
				//Locale german -> Set connectionUrl to german url
				connectionUrl = "https://www.agnitas.de/openemm-login/";
			} else {
				//Locale english -> Set connectionUrl to english url
				connectionUrl = "https://www.agnitas.de/en/openemm-login/";
			}
			//Try connection
			URL logonIframeUrl = new URL(connectionUrl);
			
			URLConnection logonIframeUrlConnection = logonIframeUrl.openConnection();
			
			logonIframeUrlConnection.connect();
			
			//Connection successful -> Return normal logon
			return connectionUrl;
		} catch (IOException e) {
			//Any connection attempt was not successful -> Return nothing
			return null;
		}
	}

    private String getLogonCompletePage(Admin admin, Model model, boolean redesigned) {
        model.addAttribute("isFrameShown", configService.getBooleanValue(ConfigValue.LoginIframe_Show, admin.getCompanyID()));
        model.addAttribute("webStorageBundleNames", getWebStorageBundleNames());
        model.addAttribute("adminId", admin.getAdminID());

        return redesigned ? "login_complete" : "logon_complete";
    }

    private List<String> getWebStorageBundleNames() {
        return WebStorageBundle.definitions().stream()
                .map(WebStorageBundle::getName)
                .collect(Collectors.toList());
    }

    protected void writeUserActivityLog(Admin admin, UserAction userAction) {
        userActivityLogService.writeUserActivityLog(admin, userAction);
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description);
    }

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return "password".equals(param);
    }
}
