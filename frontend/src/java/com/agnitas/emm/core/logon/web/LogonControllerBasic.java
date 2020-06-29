/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.logon.beans.LogonState;
import com.agnitas.emm.core.logon.forms.LogonForm;
import com.agnitas.emm.core.logon.forms.LogonHostAuthenticationForm;
import com.agnitas.emm.core.logon.forms.LogonPasswordChangeForm;
import com.agnitas.emm.core.logon.forms.LogonResetPasswordForm;
import com.agnitas.emm.core.logon.forms.validation.LogonFormValidator;
import com.agnitas.emm.core.logon.service.ComHostAuthenticationService;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.emm.core.logon.service.HostAuthenticationServiceException;
import com.agnitas.emm.core.logon.service.Logon;
import com.agnitas.emm.core.logon.service.UnexpectedLogonStateException;
import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.Anonymous;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.service.WebStorageBundle;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.UserActivityLogActions;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.Globals;
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

public class LogonControllerBasic {
    private static final Logger logger = Logger.getLogger(LogonControllerBasic.class);

    protected static final String PASSWORD_CHANGED_KEY = "com.agnitas.emm.core.logon.web.PASSWORD_CHANGED";
    protected static final String PASSWORD_RESET_LINK_PATTERN = "/logon/reset-password.action?username={username}&token={token}";

    protected ComLogonService logonService;
    protected LoginTrackService loginTrackService;
    protected ComHostAuthenticationService hostAuthenticationService;
    protected WebStorage webStorage;
    protected ConfigService configService;
    protected UserActivityLogService userActivityLogService;

    private final LogonFormValidator logonFormValidator = new LogonFormValidator();

    public LogonControllerBasic(ComLogonService logonService, LoginTrackService loginTrackService, ComHostAuthenticationService hostAuthenticationService, WebStorage webStorage, ConfigService configService, UserActivityLogService userActivityLogService) {
        this.logonService = logonService;
        this.loginTrackService = loginTrackService;
        this.hostAuthenticationService = hostAuthenticationService;
        this.webStorage = webStorage;
        this.configService = configService;
        this.userActivityLogService = userActivityLogService;
    }

    // This exception should normally never occur unless user manually navigates to some endpoints.
    @ExceptionHandler(UnexpectedLogonStateException.class)
    public String onUnexpectedLogonStateException(UnexpectedLogonStateException e) {
        logger.debug("Unexpected logon state", e);

        return "redirect:/logon.action";
    }

    @ExceptionHandler(HostAuthenticationServiceException.class)
    public String onHostAuthenticationServiceException(HostAuthenticationServiceException e, Popups popups) {
        logger.error("Host authentication exception", e);
        popups.alert("Error");

        return "redirect:/logon.action";
    }

    @Anonymous
    @PostMapping("/logon.action")
    public String logon(Logon logon, @ModelAttribute("form") LogonForm form, Model model, HttpServletRequest request, Popups popups) {
        if(!logonFormValidator.validate(form, popups)) {
            return getLogonPage(model, request.getServerName());
        }

        String clientIp = request.getRemoteAddr();
        ServiceResult<ComAdmin> result = logonService.authenticate(form.getUsername(), form.getPassword(), clientIp);

        if (result.isSuccess()) {
            ComAdmin admin = result.getResult();
            logon.authenticate(admin);

            HttpSession session = request.getSession();
            session.setAttribute(Globals.LOCALE_KEY, admin.getLocale());  // To be removed when Struts message tags are not in use anymore.

            String description = "log in IP: " + clientIp + " SessionID: " + session.getId();
            writeUserActivityLog(admin, new UserAction(UserActivityLogActions.LOGIN_LOGOUT.getLocalValue(), description));

            return "forward:/logon/authenticate-host.action";
        } else {
            form.setPassword(null);

            popups.addPopups(result);
            return getLogonPage(model, request.getServerName());
        }
    }

    @Anonymous
    @GetMapping("/logon.action")
    public String logonView(Logon logon, @ModelAttribute("form") LogonForm form, Model model, HttpServletRequest request, Popups popups) {
        SimpleServiceResult result = logonService.checkDatabase();

        if (result.isSuccess()) {
            logon.initialize(hostAuthenticationService::createHostId);
            return getLogonPage(model, request.getServerName());
        } else {
            popups.addPopups(result);
            model.addAttribute("supportEmergencyUrl", configService.getValue(ConfigValue.SupportEmergencyUrl));
            return "logon_db_failure";
        }
    }
    
    @Anonymous
    @GetMapping("/logonoffline.action")
    public String logonUrlOffline(Logon logon, @ModelAttribute("form") LogonForm form, Model model, HttpServletRequest request, Popups popups) {
        return "logon_offline";
    }

    @Anonymous
    @PostMapping("/logon/authenticate-host.action")
    public String hostAuthentication(Logon logon, @ModelAttribute("form") LogonHostAuthenticationForm form, RedirectAttributes redirectModel, Popups popups) throws HostAuthenticationServiceException {
        ComAdmin admin = logon.getAdmin();

        if (logon.is(LogonState.HOST_AUTHENTICATION_SECURITY_CODE)) {
            String authenticationCode = StringUtils.trim(form.getAuthenticationCode());
            String hostId = logon.getHostId();

            // Check if user submitted a valid authentication code.
            if (authenticateHost(admin, hostId, authenticationCode, popups)) {
                logon.authenticateHost(hostId);
            } else {
                redirectModel.addFlashAttribute("form", form);
                return "redirect:/logon/authenticate-host.action";
            }
        } else {
            logon.require(LogonState.HOST_AUTHENTICATION);

            // Simply skip this step if host authentication is not enabled.
            if (hostAuthenticationService.isHostAuthenticationEnabled(admin.getCompanyID())) {
                String hostId = logon.getCookieHostId();

                logger.info("Host authentication is ENABLED for company of user " + admin.getUsername());

                // Check if a given hostId is marked as authenticated.
                if (authenticateHost(admin, hostId)) {
                    logon.authenticateHost(hostId);
                } else {
                    // The hostId is unknown so should be confirmed via email.
                    logon.expectHostAuthenticationCode();

                    String email = getEmailForHostAuthentication(admin);
                    try {
                        // Admin/supervisor must have an e-mail address where a security code is going to be sent.
                        if (StringUtils.isBlank(email)) {
                            popups.alert("logon.error.hostauth.no_address");
                            
                            return "redirect:/logon.action";
                        } else {
                            if (hostId == null) {
                                // If hostId is missing from cookies the cookies are probably disabled.
                                popups.warning("logon.hostauth.cookies_disabled");
                            }

                            // Take hostId from cookies or session (if any), generate a new one otherwise.
                            hostAuthenticationService.sendSecurityCode(admin, logon.getHostId(hostAuthenticationService::createHostId));
                        }
                    } catch (CannotSendSecurityCodeException e) {
                        logger.error("Cannot send security code to " + e.getReceiver());
                        popups.alert("logon.error.hostauth.send_failed", email);
                        return "redirect:/logon.action";
                    } catch (Exception e) {
                        logger.error("Error generating or sending security code", e);
                        popups.alert("logon.error.hostauth.send_failed", email);
                        return "redirect:/logon.action";
                    }

                    return "redirect:/logon/authenticate-host.action";
                }
            } else {
                // Host authentication is disabled for company of user. Skip this step.
                logger.info("Host authentication is DISABLED for company of user " + admin.getUsername());

                logon.authenticateHost();
            }
        }

        return "forward:/logon/maintain-password.action";
    }

    @Anonymous
    @GetMapping("/logon/authenticate-host.action")
    public String hostAuthenticationAskSecurityCode(Logon logon, @ModelAttribute("form") LogonHostAuthenticationForm form, Model model, HttpServletRequest request) {
        logon.require(LogonState.HOST_AUTHENTICATION_SECURITY_CODE);

        model.addAttribute("adminMailAddress", getEmailForHostAuthentication(logon.getAdmin()));
        model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));

        return "logon_host_authentication";
    }

    @Anonymous
    @PostMapping("/logon/maintain-password.action")
    public String maintainPassword(Logon logon) {
        logon.require(LogonState.MAINTAIN_PASSWORD);

        PasswordState state = logonService.getPasswordState(logon.getAdmin());

        if (state != PasswordState.VALID) {
            logon.expectPasswordChange();
            return "redirect:/logon/change-password.action";
        }

        logon.complete();
        return "forward:/start.action";
    }

    @Anonymous
    @PostMapping("/logon/change-password.action")
    public String changePassword(Logon logon, @ModelAttribute("form") LogonPasswordChangeForm form, RedirectAttributes model, Popups popups) {
        logon.require(LogonState.CHANGE_ADMIN_PASSWORD, LogonState.CHANGE_SUPERVISOR_PASSWORD);

        ComAdmin admin = logon.getAdmin();

        if (form.isSkip()) {
            PasswordState state = logonService.getPasswordState(admin);

            // In some cases a password change cannot be skipped.
            if (state == PasswordState.VALID || state == PasswordState.EXPIRING) {
                logon.complete();
                return "forward:/start.action";
            }
        } else {
            SimpleServiceResult result = logonService.setPassword(admin, form.getPassword());

            if (result.isSuccess()) {
                logon.complete();
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
    public String changePassword(Logon logon, @ModelAttribute("form") LogonPasswordChangeForm form, Model model, HttpServletRequest request) {
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));

        if (model.containsAttribute(PASSWORD_CHANGED_KEY)) {
            logon.require(LogonState.COMPLETE);
            return "logon_password_changed";
        } else {
            logon.require(LogonState.CHANGE_ADMIN_PASSWORD, LogonState.CHANGE_SUPERVISOR_PASSWORD);

            ComAdmin admin = logon.getAdmin();
            PasswordState state = logonService.getPasswordState(admin);

            // Expiration date is only required if password is already expired or a "deadline" is coming.
            if (state == PasswordState.EXPIRING || state == PasswordState.EXPIRED || state == PasswordState.EXPIRED_LOCKED) {
                Date expirationDate = logonService.getPasswordExpirationDate(admin);

                if (expirationDate != null) {
                    model.addAttribute("expirationDate", admin.getDateFormat().format(expirationDate));
                }
            }

            model.addAttribute("helplanguage", logonService.getHelpLanguage(admin));
            model.addAttribute("isSupervisor", admin.isSupervisor());
            model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));

            if (state == PasswordState.EXPIRED_LOCKED) {
                return "logon_password_expired_locked";
            } else {
                model.addAttribute("isExpiring", state == PasswordState.EXPIRING);
                model.addAttribute("isExpired", state == PasswordState.EXPIRED || state == PasswordState.ONE_TIME);

                return "logon_password_change";
            }
        }
    }

    @Anonymous
    @PostMapping("/start.action")
    public String start(Logon logon, ComAdmin admin, ComAdminPreferences preferences, @RequestParam(required = false) String webStorageJson, Model model, Popups popups) {
        if (admin == null) {
            logon.require(LogonState.COMPLETE);

            if (webStorageJson == null) {
                return getLogonCompletePage(logon.getAdmin(), model);
            }

            // Finalize logon procedure, drop temporary data, setup session attributes.
            return complete(logon.end(), webStorageJson, popups);
        } else {
            // Redirect to EMM start page.
            return getStartPageRedirection(admin, preferences);
        }
    }

    @Anonymous
    @GetMapping("/start.action")
    public String startView(Logon logon, ComAdmin admin, ComAdminPreferences preferences, Model model) {
        if (admin == null) {
            logon.require(LogonState.COMPLETE);
            return getLogonCompletePage(logon.getAdmin(), model);
        } else {
            // Admin is already in, redirect to EMM start page.
            return getStartPageRedirection(admin, preferences);
        }
    }

    @Anonymous
    @PostMapping("/logon/reset-password.action")
    public String resetPassword(Logon logon, @ModelAttribute("form") LogonResetPasswordForm form, RedirectAttributes model, Popups popups, HttpServletRequest request) throws HostAuthenticationServiceException {
        String clientIp = request.getRemoteAddr();

        if (StringUtils.isNotEmpty(form.getUsername()) && StringUtils.isNotEmpty(form.getToken())) {
            ServiceResult<ComAdmin> result = logonService.resetPassword(form.getUsername(), form.getToken(), form.getPassword(), clientIp);

            if (result.isSuccess()) {
                ComAdmin admin = result.getResult();

                // Mark this host as authenticated (if authentication is enabled).
                if (hostAuthenticationService.isHostAuthenticationEnabled(admin.getCompanyID())) {
                    // Take hostId from cookies or session (if any), generate a new one otherwise.
                    String hostId = logon.getHostId(hostAuthenticationService::createHostId);

                    hostAuthenticationService.writeHostAuthentication(admin, hostId);
                }

                logon.complete(admin);

                writeUserActivityLog(admin, "change password", admin.getUsername() + " (" + admin.getAdminID() + ")");
                writeUserActivityLog(admin, UserActivityLogActions.LOGIN_LOGOUT.getLocalValue(), "logged in after password reset via " + admin.getEmail() + " from " + AgnUtils.getIpAddressForStorage(request));

                return "forward:/start.action";
            }

            popups.addPopups(result);
        } else {
            SimpleServiceResult result = logonService.requestPasswordReset(form.getUsername(), form.getEmail(), clientIp, PASSWORD_RESET_LINK_PATTERN);

            if (result.isSuccess()) {
                model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));
                return "logon_password_reset_requested";
            }

            popups.addPopups(result);
        }

        model.addFlashAttribute("form", form);

        return "redirect:/logon/reset-password.action";
    }

    @Anonymous
    @GetMapping("/logon/reset-password.action")
    public String resetPasswordView(Logon logon, @ModelAttribute("form") LogonResetPasswordForm form, Model model, HttpServletRequest request) {
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));
        model.addAttribute("helplanguage", logonService.getHelpLanguage(null));

        if (StringUtils.isNotEmpty(form.getUsername()) && StringUtils.isNotEmpty(form.getToken())) {
            return "logon_password_reset";
        } else {
            ComAdmin admin = logon.getAdmin();

            if (admin != null && StringUtils.isEmpty(form.getUsername()) && StringUtils.isEmpty(form.getEmail())) {
                form.setUsername(admin.getUsername());
                form.setEmail(admin.getEmail());
            }

            return "logon_password_reset_request";
        }
    }

    @Anonymous
    @PostMapping("/logout.action")
    public String logout(ComAdmin admin, HttpSession session) {
        // Invalidate existing session and create a new one (in order to clear stored attributes).
        session.invalidate();

        if (admin != null) {
            logger.info("User " + admin.getUsername() + " logged off");

            writeUserActivityLog(admin, new UserAction(UserActivityLogActions.LOGIN_LOGOUT.getLocalValue(), "log out"));
        }

        return "redirect:/logout.action";
    }

    @Anonymous
    @GetMapping("/logout.action")
    public String logoutView(ComAdmin admin, Model model, HttpServletRequest request) {
        if (admin == null) {
            model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));
            model.addAttribute("layoutdir", logonService.getLayoutDirectory(request.getServerName()));
            return "logged_out";
        } else {
            return "logout";
        }
    }

    private String getEmailForHostAuthentication(ComAdmin admin) {
        Supervisor supervisor = admin.getSupervisor();

        if (supervisor == null) {
            return admin.getEmail();
        }

        return supervisor.getEmail();
    }

    private boolean authenticateHost(ComAdmin admin, String hostId) throws HostAuthenticationServiceException {
        if (StringUtils.isEmpty(hostId)) {
            logger.info("Missing host ID cookie - host is assumed to be not authenticated!");
            return false;
        }

        if (hostAuthenticationService.isHostAuthenticated(admin, hostId)) {
            // Host is authenticated for user, so we can proceed to next login step
            logger.info("Host is already authenticated for user " + admin.getUsername());
            hostAuthenticationService.writeHostAuthentication(admin, hostId);
            return true;
        } else {
            logger.info("Host is not authenticated for user " + admin.getUsername());
            return false;
        }
    }

    private boolean authenticateHost(ComAdmin admin, String hostId, String authenticationCode, Popups popups) throws HostAuthenticationServiceException {
        Supervisor supervisor = admin.getSupervisor();
        String expectedCode = hostAuthenticationService.getPendingSecurityCode(admin, hostId);

        if (StringUtils.isBlank(authenticationCode)) {
            popups.field("authenticationCode", "logon.error.hostauth.empty_code");
            return false;
        }

        // Check authentication code
        if (authenticationCode.equals(expectedCode)) {
            if (supervisor == null) {
                logger.info("Authentication code correct for admin " + admin.getAdminID() + " on host " + hostId);
            } else {
                logger.info("Authentication code correct for supervisor " + supervisor.getId() + " on host " + hostId);
            }

            hostAuthenticationService.writeHostAuthentication(admin, hostId);

            return true;
        } else {
            popups.field("authenticationCode", "logon.error.hostauth.invalid_code");

            if (supervisor == null) {
                logger.info("Invalid authentication code for admin " + admin.getAdminID() + " on host " + hostId);
            } else {
                logger.info("Invalid authentication code for supervisor " + supervisor.getId() + " on host " + hostId);
            }

            return false;
        }
    }

    private String complete(final ComAdmin admin, final String webStorageJson, final Popups popups) {
        final ComAdminPreferences preferences = logonService.getPreferences(admin);
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        attributes.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN, admin, RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES, preferences, RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute(Globals.LOCALE_KEY, admin.getLocale(), RequestAttributes.SCOPE_SESSION);  // To be removed when Struts message tags are not in use anymore.
        attributes.setAttribute("emmLayoutBase", logonService.getEmmLayoutBase(admin), RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute("helplanguage", logonService.getHelpLanguage(admin), RequestAttributes.SCOPE_SESSION);

        attributes.setAttribute("userName", StringUtils.defaultString(admin.getUsername()), RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute("firstName", StringUtils.defaultString(admin.getFirstName()), RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute("fullName", admin.getFullname(), RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute("companyShortName", admin.getCompany().getShortname(), RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute("companyID", admin.getCompany().getId(), RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute("adminTimezone", admin.getAdminTimezone(), RequestAttributes.SCOPE_SESSION);

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
        
        return getStartPageRedirection(admin, preferences);
    }

    private String getLogonPage(Model model, String serverName) {
        model.addAttribute("supportMailAddress", configService.getValue(ConfigValue.Mailaddress_Support));
        model.addAttribute("iframeUrl", getLogonIframeUrl());
        model.addAttribute("layoutdir", logonService.getLayoutDirectory(serverName));
        return "logon";
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

    private String getLogonCompletePage(ComAdmin admin, Model model) {
        model.addAttribute("isFrameShown", configService.getBooleanValue(ConfigValue.LoginIframe_Show, admin.getCompanyID()));
        model.addAttribute("webStorageBundleNames", getWebStorageBundleNames());
        return "logon_complete";
    }

    private String getStartPageRedirection(ComAdmin admin, ComAdminPreferences preferences) {
        switch (preferences.getStartPage()) {
            case ComAdminPreferences.START_PAGE_DASHBOARD:
                return "redirect:/dashboard.action";

            case ComAdminPreferences.START_PAGE_CALENDAR:
                return "redirect:/calendar.action";

            default:
                throw new IllegalStateException(String.format("Unknown start page %d configured for user %s (company #%d)", preferences.getStartPage(), admin.getUsername(), admin.getCompanyID()));
        }
    }

    private List<String> getWebStorageBundleNames() {
        return WebStorageBundle.definitions().stream()
                .map(WebStorageBundle::getName)
                .collect(Collectors.toList());
    }

    private void writeUserActivityLog(ComAdmin admin, UserAction userAction) {
        userActivityLogService.writeUserActivityLog(admin, userAction);
    }

    private void writeUserActivityLog(ComAdmin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description);
    }
}
