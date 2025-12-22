/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.web;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminPreferences;
import com.agnitas.emm.common.LicenseType;
import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.commons.password.policy.PasswordPolicies;
import com.agnitas.emm.core.commons.password.util.PasswordPolicyUtil;
import com.agnitas.emm.core.loginmanager.service.LoginTrackService;
import com.agnitas.emm.core.logon.beans.LogonState;
import com.agnitas.emm.core.logon.beans.LogonStateBundle;
import com.agnitas.emm.core.logon.forms.LogonForm;
import com.agnitas.emm.core.logon.forms.LogonHostAuthenticationForm;
import com.agnitas.emm.core.logon.forms.LogonPasswordChangeForm;
import com.agnitas.emm.core.logon.forms.LogonResetPasswordForm;
import com.agnitas.emm.core.logon.forms.LogonTotpForm;
import com.agnitas.emm.core.logon.forms.validation.LogonFormValidator;
import com.agnitas.emm.core.logon.service.ClientHostIdService;
import com.agnitas.emm.core.logon.service.HostAuthenticationService;
import com.agnitas.emm.core.logon.service.HostAuthenticationServiceException;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.logon.service.UnexpectedLogonStateException;
import com.agnitas.emm.core.sessionhijacking.web.SessionHijackingPreventionConstants;
import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.security.sessionbinding.web.service.SessionBindingService;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.service.WebStorageBundle;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.UserActivityLogActions;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.Anonymous;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class LogonController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(LogonController.class);

    public static final String PASSWORD_RESET_LINK_PATTERN = "/logon/reset-password.action?username={username}&token={token}";
    private static final String PASSWORD_CHANGED_KEY = "com.agnitas.emm.core.logon.web.PASSWORD_CHANGED";
    private static final String REDIRECT_TO_AUTHENTICATE_HOST = "redirect:/logon/authenticate-host.action";
    private static final String REDIRECT_TO_MAINTAIN_PASSWORD = "redirect:/logon/maintain-password.action";
    private static final String REDIRECT_TO_START = "redirect:/dashboard.action";
    private static final String REDIRECT_TO_RESET_PASSWORD = "redirect:/logon/reset-password.action";
    private static final String REDIRECT_TO_LOGON = "redirect:/logon.action";

    protected final LogonService logonService;
    protected final ConfigService configService;
    private final LoginTrackService loginTrackService;
    private final HostAuthenticationService hostAuthenticationService;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;
    private final ClientHostIdService clientHostIdService;
    private final SessionBindingService sessionBindingService;

    private final LogonFormValidator logonFormValidator = new LogonFormValidator();

    public LogonController(LogonService logonService, LoginTrackService loginTrackService, HostAuthenticationService hostAuthenticationService, WebStorage webStorage, ConfigService configService, UserActivityLogService userActivityLogService, ClientHostIdService clientHostIdService, SessionBindingService sessionBindingService) {
        this.logonService = logonService;
        this.loginTrackService = loginTrackService;
        this.hostAuthenticationService = hostAuthenticationService;
        this.webStorage = webStorage;
        this.configService = configService;
        this.userActivityLogService = userActivityLogService;
        this.clientHostIdService = Objects.requireNonNull(clientHostIdService, "ClientHostIdService is null");
        this.sessionBindingService = Objects.requireNonNull(sessionBindingService, "session binding service");
    }

    // This exception should normally never occur unless user manually navigates to some endpoints.
    @ExceptionHandler(UnexpectedLogonStateException.class)
    public String onUnexpectedLogonStateException(UnexpectedLogonStateException e) {
        logger.debug("Unexpected logon state", e);
        return REDIRECT_TO_LOGON;
    }

    @ExceptionHandler(HostAuthenticationServiceException.class)
    public String onHostAuthenticationServiceException(HostAuthenticationServiceException e, Popups popups) {
        logger.error("Host authentication exception", e);
        popups.defaultError();

        return REDIRECT_TO_LOGON;
    }

    @Anonymous
    @GetMapping("/logon.action")
    public String logonView(LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonForm form,
                            @RequestParam(value = "afterLogout", required = false) boolean isAfterLogout, Model model,
                            HttpServletRequest request, HttpServletResponse response, Popups popups, HttpSession session) {
        if(session.getAttribute(SessionHijackingPreventionConstants.FORCED_LOGOUT_MARKER_ATTRIBUTE_NAME) != null) {
            popups.alert(new Message("logon.security.sessionCheck"));
            session.removeAttribute(SessionHijackingPreventionConstants.FORCED_LOGOUT_MARKER_ATTRIBUTE_NAME);
        }

        this.sessionBindingService.bindSession(request, response);

        SimpleServiceResult result = logonService.checkDatabase();

        if (result.isSuccess()) {
            if (logonStateBundle.shouldPrefillUsername()) {
                form.setUsername(logonService.getLoginUsername(logonStateBundle.getAdmin()));
            }

            logonStateBundle.toPendingState();
            model.addAttribute("afterLogout", isAfterLogout);
            return getLogonPage(model, request.getServerName(), request);
        }

        popups.addPopups(result);
        model.addAttribute("supportEmergencyUrl", configService.getValue(ConfigValue.SupportEmergencyUrl));
        return "login_db_failure";
    }

    @Anonymous
    @PostMapping("/logon.action")
    public String logon(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonForm form, Model model, HttpServletRequest request, Popups popups) {
        if (!logonFormValidator.validate(form, popups)) {
            return getLogonPage(model, request.getServerName(), request);
        }

        String clientIp = request.getRemoteAddr();
        ServiceResult<Admin> result = logonService.authenticate(form.getUsername(), form.getPassword(), clientIp);

        if (result.isSuccess()) {
            Admin admin = result.getResult();

            if (admin.isRestful()) {
                // Restfull webservice user may not logon via GUI
                form.setPassword(null);
                popups.alert("error.admin.gui.locked", admin.getUsername());
                return getLogonPage(model, request.getServerName(), request);
            }

            if (!admin.isSupervisor() && !this.configService.getBooleanValue(ConfigValue.LogonAllowUILoginByUsernameAndPassword, admin.getCompanyID())) {
                logger.info("Login to UI by username and password is disabled for company {}", admin.getCompanyID());

                form.setPassword(null);
                popups.alert("error.login", admin.getUsername());
                return getLogonPage(model, request.getServerName(), request);
            }

            logonStateBundle.toTotpState(admin);

            String description = "log in IP: " + clientIp + " SessionID: " + request.getSession().getId();
            writeUserActivityLog(admin, new UserAction(UserActivityLogActions.LOGIN_LOGOUT.getLocalValue(), description));

            return "redirect:/logon/totp.action";
        }

        form.setPassword(null);

        popups.addPopups(result);
        return getLogonPage(model, request.getServerName(), request);
    }

    @Anonymous
    @GetMapping("/logon/totp.action")
    public String totpShowForm(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonTotpForm form, final Model model, final HttpServletRequest request) {
        return doTotpShowForm(logonStateBundle, form, logonStateBundle.getAdmin(), model, request);
    }
    
    protected String doTotpShowForm(LogonStateBundle logonStateBundle, LogonTotpForm form, Admin admin, Model model, HttpServletRequest request) {
    	logonStateBundle.toAuthenticationState();
    	return REDIRECT_TO_AUTHENTICATE_HOST;
    }

    @Anonymous
    @PostMapping("/logon/totp.action")
    public String totpVerifyValue(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonTotpForm form, final Popups popups, final HttpServletResponse response) {
        return doTotpVerifyValue(logonStateBundle, form, logonStateBundle.getAdmin(), popups, response);
    }
    
    protected String doTotpVerifyValue(LogonStateBundle logonStateBundle, LogonTotpForm form, Admin admin, Popups popups, HttpServletResponse response) {
    	logonStateBundle.toAuthenticationState();
    	return REDIRECT_TO_AUTHENTICATE_HOST;
    }
    
    @Anonymous
    @GetMapping("/logonoffline.action")
    public String logonUrlOffline(@ModelAttribute("form") LogonForm form) {
        return "login_offline";
    }

    @Anonymous
    @PostMapping("/logon/authenticate-host.action")
    public String hostAuthentication(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonHostAuthenticationForm form, RedirectAttributes redirectModel, Popups popups, final HttpServletResponse response) throws HostAuthenticationServiceException {
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
            return REDIRECT_TO_MAINTAIN_PASSWORD;
        } else {
            redirectModel.addFlashAttribute("form", form);
            return REDIRECT_TO_AUTHENTICATE_HOST;
        }
    }

    @Anonymous
    @GetMapping("/logon/authenticate-host.action")
    public String hostAuthenticationAskSecurityCode(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonHostAuthenticationForm form, Model model, HttpServletRequest request, final Popups popups) throws HostAuthenticationServiceException {
        final Admin admin = logonStateBundle.getAdmin();

        // Simply skip this step if host authentication is not enabled.
        if (!hostAuthenticationService.isHostAuthenticationEnabled(admin.getCompanyID())) {
            // Host authentication is disabled for company of user. Skip this step.
            logger.info("Host authentication is DISABLED for company of user {}", admin.getUsername());

            logonStateBundle.toMaintainPasswordState();
            return REDIRECT_TO_MAINTAIN_PASSWORD;
        }

        logger.info("Host authentication is ENABLED for company of user {}", admin.getUsername());
        final String hostId = this.clientHostIdService.getClientHostId(request)
                .orElseGet(clientHostIdService::createHostId);

        // Check if a given hostId is marked as authenticated.
        if (authenticateHost(admin, hostId)) {
            logonStateBundle.toMaintainPasswordState();
            return REDIRECT_TO_MAINTAIN_PASSWORD;
        }

        PasswordState state = logonService.getPasswordState(admin);
        if (state.equals(PasswordState.EXPIRED_LOCKED)) {
            return showPasswordExpiredPage(admin, popups);
        }

        // The hostId is unknown so should be confirmed via email.
        logonStateBundle.toAuthenticateHostSecurityCodeState(hostId);

        String email = getEmailForHostAuthentication(admin);

        // Admin/supervisor must have an e-mail address where a security code is going to be sent.
        if (StringUtils.isBlank(email)) {
            popups.alert("logon.error.hostauth.no_address");
            return REDIRECT_TO_LOGON;
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
            return REDIRECT_TO_LOGON;
        } catch (Exception e) {
            logger.error("Error generating or sending security code", e);
            popups.alert("logon.error.hostauth.send_failed", email);
            return REDIRECT_TO_LOGON;
        }

        model.addAttribute("adminMailAddress", email);
        model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));

        return "login_host_authentication";
    }

    @Anonymous
    @GetMapping("/logon/maintain-password.action")
    public String maintainPassword(LogonStateBundle logonStateBundle, Popups popups) {
        logonStateBundle.requireLogonState(LogonState.MAINTAIN_PASSWORD);

        PasswordState state = logonService.getPasswordState(logonStateBundle.getAdmin());

        if (state != PasswordState.VALID) {
            if (state.equals(PasswordState.EXPIRED_LOCKED)) {
                return showPasswordExpiredPage(logonStateBundle.getAdmin(), popups);
            }

            logonStateBundle.toPasswordChangeState();
            return "redirect:/logon/change-password.action";
        }

        logonStateBundle.toCompleteState();
        return "forward:/start.action";
    }

    @Anonymous
    @PostMapping("/logon/change-password.action")
    public String changePassword(LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonPasswordChangeForm form, RedirectAttributes model, Popups popups) {
        logonStateBundle.requireLogonState(LogonState.CHANGE_ADMIN_PASSWORD, LogonState.CHANGE_SUPERVISOR_PASSWORD);

        Admin admin = logonStateBundle.getAdmin();

        if (form.isSkip()) {
            PasswordState state = logonService.getPasswordState(admin);

            // In some cases a password change cannot be skipped.
            if (state == PasswordState.VALID || state == PasswordState.EXPIRING) {
                logonStateBundle.toCompleteState(true);
                return "forward:/start.action";
            }
        } else {
            SimpleServiceResult result = logonService.setPassword(admin, form.getPassword());

            if (result.isSuccess()) {
                logonStateBundle.toCompleteState(true);
                // Show success page which indicates that password has been changed.
                model.addFlashAttribute(PASSWORD_CHANGED_KEY, true);
            } else {
                popups.addPopups(result);
                // Show password change page and the error message(s).
                model.addFlashAttribute("isAnotherAttempt", true);
            }
        }

        return "redirect:/logon/change-password.action";
    }

    @Anonymous
    @GetMapping("/logon/change-password.action")
    public String changePassword(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonPasswordChangeForm form, Model model, Popups popups, HttpServletRequest request) {
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));

        if (model.containsAttribute(PASSWORD_CHANGED_KEY)) {
            logonStateBundle.requireLogonState(LogonState.COMPLETE);
            popups.success("password.changed.proceed", request.getContextPath() + "/logon.action");
            return "login_password_changed";
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

            model.addAttribute("passwordPolicy", getPasswordPolicy(admin).getPolicyName());

            return "login_password_change";
        }
    }

    protected PasswordPolicies getPasswordPolicy(Admin admin) {
        return PasswordPolicyUtil.loadCompanyPasswordPolicy(admin.getCompanyID(), configService);
    }

    private String showPasswordExpiredPage(Admin admin, Popups popups) {
        Date expirationDate = logonService.getPasswordExpirationDate(admin);

        String errorMessage = I18nString.getLocaleString("password.change.notification.expired", admin.getLocale(), admin.getDateFormat().format(expirationDate));
        if (!admin.isSupervisor()) {
            errorMessage += "<br/>" + I18nString.getLocaleString("password.finally.expired.info", admin.getLocale());
        }

        popups.exactAlert(errorMessage);
        return "login_password_expired_locked";
    }

    @Anonymous
    @PostMapping("/start.action")
    public String start(LogonStateBundle logonStateBundle, Admin admin, @RequestParam(required = false) String webStorageJson,
                                  Model model, Popups popups, HttpServletRequest request, HttpServletResponse response) {
        if (admin == null) {
            logonStateBundle.requireLogonState(LogonState.COMPLETE);

            if (webStorageJson == null) {
                return getLogonCompletePage(logonStateBundle.getAdmin(), model);
            }

            // Finalize logon procedure, drop temporary data, setup session attributes.
            return complete(logonStateBundle.toCompleteState(request), webStorageJson, popups, request, response);
        }

        // Redirect to EMM start page.
        return REDIRECT_TO_START;
    }

    @Anonymous
    @GetMapping("/start.action")
    public String startView(final LogonStateBundle logonStateBundle, Admin admin, Model model) {
        if (admin == null) {
            // No need to check login state here. This GET requests just displays the login form.
            return getLogonCompletePage(logonStateBundle.getAdmin(), model);
        }

        // Admin is already in, redirect to EMM start page.
        return REDIRECT_TO_START;
    }

    @Anonymous
    @PostMapping("/logon/reset-password.action")
    public String resetPassword(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonResetPasswordForm form, RedirectAttributes model, Popups popups, HttpServletRequest request) throws HostAuthenticationServiceException {
        String clientIp = request.getRemoteAddr();

        if (StringUtils.isNotEmpty(form.getUsername()) && StringUtils.isNotEmpty(form.getToken())) {
            ServiceResult<Admin> result = logonService.resetPassword(form.getUsername(), form.getToken(), form.getPassword(), clientIp);

            if (result.isSuccess()) {
                Admin admin = result.getResult();

                // Mark this host as authenticated (if authentication is enabled).
                if (hostAuthenticationService.isHostAuthenticationEnabled(admin.getCompanyID())) {
                    // Take hostId from cookies or session (if any), generate a new one otherwise.
                    final String hostId = this.clientHostIdService.getClientHostId(request)
                            .orElseGet(clientHostIdService::createHostId);

                    hostAuthenticationService.writeHostAuthentication(admin, hostId);
                }

                logonStateBundle.toCompleteState(admin);

                writeUserActivityLog(admin, "change password", admin.getUsername() + " (" + admin.getAdminID() + ")");
                writeUserActivityLog(admin, UserActivityLogActions.LOGIN_LOGOUT.getLocalValue(), "logged in after password reset via " + admin.getEmail() + " from " + AgnUtils.getIpAddressForStorage(request));

                return "forward:/logon.action";
            }

            popups.addPopups(result);
        } else {
            SimpleServiceResult result = logonService.requestPasswordReset(form.getUsername(), form.getEmail(), clientIp, PASSWORD_RESET_LINK_PATTERN);
            popups.addPopups(result);
        }

        model.addFlashAttribute("form", form);

        return REDIRECT_TO_RESET_PASSWORD;
    }

    @Anonymous
    @GetMapping("/logon/reset-password.action")
    public String resetPasswordView(final LogonStateBundle logonStateBundle, @ModelAttribute("form") LogonResetPasswordForm form, Model model, Popups popups, HttpServletRequest request) {
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));
        model.addAttribute("helplanguage", logonService.getHelpLanguage(null));

        if (StringUtils.isNotEmpty(form.getUsername()) && StringUtils.isNotEmpty(form.getToken())) {
            if (!logonService.existsPasswordResetTokenHash(form.getUsername(), form.getToken())) {
                logonService.riseErrorCount(form.getUsername());
                popups.alert("error.passwordReset.auth");
                return REDIRECT_TO_RESET_PASSWORD;
            }

            if (!logonService.isValidPasswordResetTokenHash(form.getUsername(), form.getToken())) {
                ServiceResult<Admin> result = new ServiceResult<>(null, false, Message.of("error.passwordReset.expired", LogonService.TOKEN_EXPIRATION_MINUTES, configService.getValue(ConfigValue.SystemUrl) + "/logon/reset-password.action"));
                popups.addPopups(result);
                return REDIRECT_TO_RESET_PASSWORD;
            }

            return "login_password_reset";
        } else {
            Admin admin = logonStateBundle.getAdmin();

            if (admin != null && StringUtils.isEmpty(form.getUsername()) && StringUtils.isEmpty(form.getEmail())) {
                form.setUsername(admin.getUsername());
                form.setEmail(admin.getEmail());
            }

            return "login_password_reset_request";
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
        }

        return "redirect:/logon.action?afterLogout=true";
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
        if (StringUtils.isBlank(authenticationCode)) {
            popups.fieldError("authenticationCode", "logon.error.hostauth.empty_code");
            return false;
        }

        Supervisor supervisor = admin.getSupervisor();
        String expectedCode = hostAuthenticationService.getPendingSecurityCode(admin, hostId);

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

    private String complete(Admin admin, String webStorageJson, Popups popups, HttpServletRequest request, HttpServletResponse response) {
        final AdminPreferences preferences = logonService.getPreferences(admin);
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        attributes.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN, admin, RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES, preferences, RequestAttributes.SCOPE_SESSION);
        this.sessionBindingService.bindSession(request, response);

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

        LicenseType licenseType = configService.getLicenseType();
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
        return REDIRECT_TO_START;
    }

    private String getLogonPage(Model model, String serverName, HttpServletRequest request) {
        model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));
        model.addAttribute("iframeUrl", logonService.getLoginIframeUrl());
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(serverName));
        model.addAttribute("SHOW_TAB_HINT", showTabHint(request));
        return "login";
    }
    
    protected boolean showTabHint(HttpServletRequest request) {
    	return !request.getServletContext().getEffectiveSessionTrackingModes().contains(SessionTrackingMode.COOKIE);
    }
    
    private String getLogonCompletePage(Admin admin, Model model) {
        model.addAttribute("webStorageBundleNames", getWebStorageBundleNames());
        model.addAttribute("adminId", admin.getAdminID());
        return "login_complete";
    }

    private List<String> getWebStorageBundleNames() {
        return WebStorageBundle.definitions().stream()
                .map(WebStorageBundle::getName)
                .toList();
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
