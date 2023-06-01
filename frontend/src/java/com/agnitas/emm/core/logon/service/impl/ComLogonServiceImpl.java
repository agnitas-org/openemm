/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.sql.DataSource;

import org.agnitas.beans.EmmLayoutBase;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminPreferences;
import com.agnitas.dao.AdminPreferencesDao;
import com.agnitas.dao.ComEmmLayoutBaseDao;
import com.agnitas.dao.PasswordResetDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.AdminException;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.password.ComPasswordCheck;
import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.emm.core.logon.service.LogonServiceException;
import com.agnitas.emm.core.logon.web.LogonFailedException;
import com.agnitas.emm.core.mailloop.util.SecurityTokenGenerator;
import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.emm.core.supervisor.common.SupervisorLoginFailedException;
import com.agnitas.emm.core.supervisor.service.ComSupervisorService;
import com.agnitas.emm.core.supervisor.service.SupervisorUtil;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.LicenseError;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;

/**
 * Implementation of {@link com.agnitas.emm.core.logon.service.ComLogonService}.
 */
public class ComLogonServiceImpl implements ComLogonService {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComLogonServiceImpl.class);

	private DataSource dataSource;
	
	private ConfigService configService;

	private AdminPreferencesDao adminPreferencesDao;

	private ComEmmLayoutBaseDao emmLayoutBaseDao;
	
	private AdminService adminService;

	private ComSupervisorService supervisorService;

	private ComPasswordCheck passwordCheck;

	private PasswordResetDao passwordResetDao;

	/** Service for login tracking. */
	private LoginTrackService loginTrackService;

	private JavaMailService javaMailService;

	protected PreviewFactory previewFactory;

	// ----------------------------------------------------------- Business code
	
	@Override
	public Admin getAdminByCredentials(String username, String password, String hostIpAddress) throws LogonServiceException {
		if (!SupervisorUtil.isSupervisorLoginName(username)) {
			return doRegularLogin(username, password, hostIpAddress);
		} else {
			return doSupervisorLogin(username, password, hostIpAddress);
		}
	}

	/**
	 * Do login for EMM user.
	 * 
	 * @param username name of EMM user
	 * @param password password
	 * @param hostIpAddress IP address of host
	 * 
	 * @return {@link Admin} for credentials
	 * 
	 * @throws LogonServiceException on errors during login (invalid username/password, ...)
	 */
	private Admin doRegularLogin( String username, String password, String hostIpAddress) throws LogonServiceException {
		final Optional<Admin> adminOptional = adminService.findAdminByCredentials(username, password);
		
		// Check, if we got an Admin (combination of username and password is valid)
		if (!adminOptional.isPresent()) {
			if (logger.isInfoEnabled()) {
				logger.info("Login for user " + username + " failed - invalid combination of username and password");
			}
			loginTrackService.trackLoginFailed( hostIpAddress, username);

			throw new LogonFailedException(false);
		}
		
		final Admin admin = adminOptional.get();
		
		// Check, if IP is currently blocked
		checkIPBlockState(admin.getCompanyID(), username, hostIpAddress);
			
		if (logger.isInfoEnabled()) {
			logger.info("Login for user " + username + " successful");
		}
		
		loginTrackService.trackLoginSuccessful(hostIpAddress, username);

		return admin;
	}

	/**
	 * Checks, if IP address is currently blocked for given admin.
	 * 
	 * @param companyID company ID of user
	 * @param loginName login name of user
	 * @param hostIpAddress IP address
	 * 
	 * @throws LogonFailedException if IP is blocked
	 */
	private void checkIPBlockState( int companyID, String loginName, String hostIpAddress) throws LogonFailedException {
		if(loginTrackService.isIpAddressLocked(hostIpAddress, companyID)) {
			if (logger.isInfoEnabled()) {
				logger.info( "Login for user " + loginName + " failed - IP address blocked");
			}
			loginTrackService.trackLoginSuccessfulButBlocked( hostIpAddress, loginName);
			
			throw new LogonFailedException(false);
		}
	}
	
	/**
	 * Do login for supervisor user.
	 * 
	 * @param loginName login name (user name + supervisor name)
	 * @param password supervisor password
	 * @param hostIpAddress IP of client
	 * 
	 * @return {@link Admin} for credentials
	 * 
	 * @throws LogonServiceException on errors during login (username/password invalid, ...)
	 */
	private Admin doSupervisorLogin(final String loginName, final String password, final String hostIpAddress) throws LogonServiceException {
		final String username = SupervisorUtil.getUserNameFromLoginName(loginName);
		final String supervisorName = SupervisorUtil.getSupervisorNameFromLoginName(loginName);
		
		// Get admin
		try {
			final Admin admin = adminService.getAdminByNameForSupervisor(username, supervisorName, password);
			
			// Check, if admin is valid
			if (admin == null || admin.getAdminID() == 0) {
				loginTrackService.trackLoginFailed(hostIpAddress, SupervisorUtil.formatCompleteName(username, supervisorName));
				
				throw new LogonFailedException(true);
			}
			
			// Check, if IP is blocked
			checkIPBlockState(admin.getCompanyID(), loginName, hostIpAddress);
			
			loginTrackService.trackLoginSuccessful(hostIpAddress, loginName);
			
			return admin;
		} catch (final SupervisorLoginFailedException e) {
			logger.warn("Supervisor logon failed", e);

			loginTrackService.trackLoginFailed(hostIpAddress, SupervisorUtil.formatCompleteName(username, supervisorName));

			throw new LogonFailedException(true);
		} catch (final AdminException | SupervisorException e) {
			logger.warn("Logon failed", e);

			throw new LogonFailedException(true);
		}
	}

	@Override
	public ServiceResult<Admin> authenticate(String username, String password, String clientIp) {
		try {
			checkLicense();
		} catch (LicenseError e) {
			return new ServiceResult<>(null, false, asMessage(e));
		}

		try {
			// This also logs login attempt!
			return new ServiceResult<>(getAdminByCredentials(username, password, clientIp), true);
		} catch (LogonFailedException e) {
			if (logger.isInfoEnabled()) {
				logger.info("Login failed for user " + username, e);
			}

			return new ServiceResult<>(null, false, asMessage(e));
		} catch (Exception e) {
			logger.error("Error during login for user " + username, e);
			return new ServiceResult<>(null, false, Message.of("error.login.general"));
		}
	}

	@Override
	public SimpleServiceResult checkDatabase() {
		try {
			DbUtilities.checkDatasourceConnection(dataSource);
			return new SimpleServiceResult(true);
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
			return new SimpleServiceResult(false, Message.exact(e.getMessage()));
		}
	}

	@Override
	public AdminPreferences getPreferences(Admin admin) {
		return adminPreferencesDao.getAdminPreferences(admin.getAdminID());
	}

	@Override
	public EmmLayoutBase getEmmLayoutBase(Admin admin) {
		return emmLayoutBaseDao.getEmmLayoutBase(admin.getCompanyID(), admin.getLayoutBaseID());
	}

	@Override
	public String getLayoutDirectory(String serverName) {
		return emmLayoutBaseDao.getLayoutDirectory(serverName);
	}

	@Override
	public String getHelpLanguage(Admin admin) {
		if (admin == null) {
			return getHelpLanguage(LocaleContextHolder.getLocale().getLanguage());
		} else {
			return getHelpLanguage(admin.getAdminLang().trim().toLowerCase());
		}
	}

	@Override
	public PasswordState getPasswordState(Admin admin) {
		if (admin.isSupervisor()) {
			return supervisorService.getPasswordState(admin.getSupervisor());
		} else {
			return adminService.getPasswordState(admin);
		}
	}

	@Override
	public Date getPasswordExpirationDate(Admin admin) {
		if (admin.isSupervisor()) {
			return supervisorService.computePasswordExpireDate(admin.getSupervisor());
		} else {
			return adminService.computePasswordExpireDate(admin);
		}
	}

	@Override
	public SimpleServiceResult setPassword(Admin admin, String password) {
		if (StringUtils.isBlank(password)) {
			return new SimpleServiceResult(false, Message.of("error.password.required"));
		}

		if (admin.isSupervisor()) {
			Supervisor supervisor = admin.getSupervisor();

			try {
				SimpleServiceResult result = passwordCheck.checkSupervisorPassword(password, supervisor);

				if (result.isSuccess()) {
					supervisorService.setSupervisorPassword(supervisor.getId(), password);
				}

				return result;
			} catch (SupervisorException e) {
				logger.error("Error occurred: " + e.getMessage(), e);
				return new SimpleServiceResult(false, Message.of("Error"));
			}
		} else {
			SimpleServiceResult result = passwordCheck.checkAdminPassword(password, admin);

			if (result.isSuccess()) {
				if (!adminService.setPassword(admin.getAdminID(), admin.getCompanyID(), password)) {
					return new SimpleServiceResult(false, Message.of("Error"));
				}
			}

			return result;
		}
	}

	@Override
	public SimpleServiceResult requestPasswordReset(String username, String email, String clientIp, String linkPattern) {
		Admin admin = getAdmin(username, email);

		if (admin == null) {
			loginTrackService.trackLoginFailed(clientIp, username);
			return new SimpleServiceResult(false, Message.of("GWUA.error.passwordReset"));
		}

		int adminId = admin.getAdminID();

		// Make sure that there's no other pending token.
		if (passwordResetDao.getPasswordResetTokenHash(adminId) != null) {
			logger.info("Multiple password reset request for " + admin.getUsername() + ". Reset is already pending.");
			return new SimpleServiceResult(false, Message.of("error.passwordReset.auth.multiple"));
		}

		// Remove token, because we didn't get one if error count was too high
		passwordResetDao.remove(adminId);
		String token = generatePasswordResetToken(admin, clientIp);
		String passwordResetLink = getPasswordResetLink(linkPattern, username, token);

		sendPasswordResetMail(admin, passwordResetLink);

		return new SimpleServiceResult(true);
	}
	
	@Override
	public ServiceResult<Admin> resetPassword(String username, String token, String password, String clientIp) {
		final Optional<Admin> adminOptional = adminService.getAdminByName(username);
		
		if(!adminOptional.isPresent()) {
			if(logger.isInfoEnabled()) {
				logger.info(String.format("Unknown username '%s'", username));
			}
			
			loginTrackService.trackLoginFailed(clientIp, username);
			
			return new ServiceResult<>(null, false, Message.of("GWUA.error.passwordReset"));
		}
		
		final Admin admin = adminOptional.get();

		if (passwordResetDao.existsPasswordResetTokenHash(username, getTokenHash(token))) {
			if (passwordResetDao.isValidPasswordResetTokenHash(username, getTokenHash(token))) {
				SimpleServiceResult result = setPassword(admin, password);
	
				if (result.isSuccess()) {
					passwordResetDao.remove(admin.getAdminID());
					return new ServiceResult<>(admin, true);
				} else {
					return new ServiceResult<>(null, false, result.getSuccessMessages(), result.getWarningMessages(), result.getErrorMessages());
				}
			} else {
				return new ServiceResult<>(null, false, Message.of("error.passwordReset.expired", TOKEN_EXPIRATION_MINUTES, configService.getValue(ConfigValue.SystemUrl) + "/logon/reset-password.action"));
			}
		} else {
			// If a password reset token is there increment errors count (to prevent brute force attack).
			// If a password reset token isn't there then a brute force attack is useless (there's no chance to hit).
			passwordResetDao.riseErrorCount(admin.getAdminID());
			return new ServiceResult<>(null, false, Message.of("error.passwordReset.auth"));
		}
	}

	@Override
	public String getPasswordResetLink(String linkPattern, String username, String token) {
		try {
			String baseUrl = configService.getValue(ConfigValue.SystemUrl);
			String link = linkPattern.replace("{token}", URLEncoder.encode(token, "UTF-8"))
					.replace("{username}", URLEncoder.encode(username, "UTF-8"));

			if (StringUtils.startsWithIgnoreCase(link, "http://") || StringUtils.startsWithIgnoreCase(link, "https://")) {
				return link;
			} else {
				return StringUtils.removeEnd(baseUrl, "/") + "/" + StringUtils.removeStart(link, "/");
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private Admin getAdmin(String username, String email) {
		final Optional<Admin> adminOptional = adminService.getAdminByName(username);
		
		if(!adminOptional.isPresent()) {
			if(logger.isInfoEnabled()) {
				logger.info(String.format("Unknown username '%s'", username));
			}
			
			return null;
		}
		
		final Admin admin = adminOptional.get();

		if (StringUtils.equalsIgnoreCase(admin.getEmail(), email)) {
			return admin;
		} else {
			return null;
		}
	}
	
	private String generatePasswordResetToken(Admin admin, String clientIp) {
		String token = SecurityTokenGenerator.generateSecurityToken();
		Date expirationDate = DateUtils.addMinutes(new Date(), TOKEN_EXPIRATION_MINUTES);

		passwordResetDao.save(admin.getAdminID(), getTokenHash(token), expirationDate, clientIp);

		return token;
	}
	
	private String generatePasswordSetToken(Admin admin, String clientIp, Date expirationDate) {
		String token = SecurityTokenGenerator.generateSecurityToken();

		passwordResetDao.save(admin.getAdminID(), getTokenHash(token), expirationDate, clientIp);

		return token;
	}

	private String getTokenHash(String token) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
			return new String(Hex.encodeHex(digest)).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendPasswordResetMail(Admin admin, String passwordResetLink) {
		Locale locale = admin.getLocale();
		
		final String mailSubject;
		final String mailContentHtml;
		final String mailContentText;
		int resetPasswordMailingID = adminService.getPasswordResetMailingId(admin.getLocale().getLanguage());
		if (resetPasswordMailingID <= 0 && !"en".equalsIgnoreCase(admin.getLocale().getLanguage())) {
			resetPasswordMailingID = adminService.getPasswordResetMailingId("en");
		}
		if (resetPasswordMailingID > 0) {
			final Preview preview = previewFactory.createPreview();
			final Page output = preview.makePreview(resetPasswordMailingID, 0, true);
			preview.done();

			mailSubject = output.getHeaderField("subject").replace("{0}", admin.getUsername()).replace("{1}", admin.getUsername()).replace("{2}", admin.getFirstName()).replace("{3}", admin.getFullname());
			mailContentText = output.getText().replace("{0}", admin.getUsername()).replace("{1}", passwordResetLink).replace("{2}", admin.getFirstName()).replace("{3}", admin.getFullname());
			mailContentHtml = output.getHTML().replace("{0}", admin.getUsername()).replace("{1}", passwordResetLink).replace("{2}", admin.getFirstName()).replace("{3}", admin.getFullname());
		} else {
			mailSubject = I18nString.getLocaleString("passwordReset.mail.subject", locale, admin.getUsername(), passwordResetLink, admin.getFirstName(), admin.getFullname());
			mailContentText = I18nString.getLocaleString("passwordReset.mail.body.text", locale, passwordResetLink, admin.getUsername(), admin.getFirstName(), admin.getFullname());
			mailContentHtml = I18nString.getLocaleString("passwordReset.mail.body.html", locale, passwordResetLink, admin.getUsername(), admin.getFirstName(), admin.getFullname());
		}
		
		javaMailService.sendEmail(admin.getCompanyID(), admin.getEmail(), mailSubject, mailContentText, mailContentHtml);
	}
	
	private void checkLicense() {
		// Read license id to check limits
		configService.getValue(ConfigValue.System_Licence);

		String expirationDate = configService.getValue(ConfigValue.System_License_ExpirationDate);
		// Make sure that the license is not expired.
		if (StringUtils.isNotBlank(expirationDate)) {
			try {
				Date date = new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).parse(expirationDate + " 23:59:59");

				if (DateUtilities.isPast(date)) {
					throw new LicenseError("error.license.outdated", expirationDate, configService.getValue(ConfigValue.Mailaddress_Support));
				}
			} catch (ParseException e) {
				throw new LicenseError("Invalid validity data: " + e.getMessage(), e);
			}
		}
	}

	private Message asMessage(LogonFailedException e) {
		if (e.isSupervisorLogon()) {
			return Message.of("error.login.supervisor");
		} else {
			return Message.of("error.login");
		}
	}

	private Message asMessage(LicenseError e) {
		if (StringUtils.equals(e.getErrorKey(), "error.license.outdated")) {
			return Message.of("error.licenseError", I18nString.getLocaleString(e.getErrorKey(), LocaleContextHolder.getLocale(), configService.getValue(ConfigValue.System_License_ExpirationDate)));
		} else {
			return Message.of("error.licenseError", e.getMessage());
		}
	}

	private String getHelpLanguage(String preferredLanguage) {
		List<String> availableLanguages = configService.getListValue(ConfigValue.OnlineHelpLanguages);

		if (availableLanguages.contains(preferredLanguage.toLowerCase())) {
			return preferredLanguage;
		} else {
			return DEFAULT_HELP_LANGUAGE;
		}
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setAdminPreferencesDao(AdminPreferencesDao dao) {
		this.adminPreferencesDao = dao;
	}

	@Required
	public void setEmmLayoutBaseDao(ComEmmLayoutBaseDao emmLayoutBaseDao) {
		this.emmLayoutBaseDao = emmLayoutBaseDao;
	}

	/**
	 * Set service for login tracking.
	 *
	 * @param service service for login tracking
	 */
	@Required
	public void setLoginTrackService( LoginTrackService service) {
		this.loginTrackService = service;
	}

	@Required
	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	@Required
	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}

	@Required
	public void setSupervisorService(ComSupervisorService supervisorService) {
		this.supervisorService = supervisorService;
	}

	@Required
	public void setPasswordCheck(ComPasswordCheck passwordCheck) {
		this.passwordCheck = passwordCheck;
	}

	@Required
	public void setPasswordResetDao(PasswordResetDao passwordResetDao) {
		this.passwordResetDao = passwordResetDao;
	}

	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Required
	public void setPreviewFactory(final PreviewFactory previewFactory) {
		this.previewFactory = previewFactory;
	}
	
	@Override
	public SimpleServiceResult sendWelcomeMail(Admin admin, String clientIp, String linkPattern) {
		Date expirationDate = DateUtils.addDays(new Date(), TOKEN_EXPIRATION_DAYS);
		String token = generatePasswordSetToken(admin, clientIp, expirationDate);
		String passwordResetLink = getPasswordResetLink(linkPattern, admin.getUsername(), token);
		Locale locale = admin.getLocale();
		
		final String mailSubject;
		final String mailContentHtml;
		final String mailContentText;
		int adminWelcomeMailingID = adminService.getAdminWelcomeMailingId(admin.getLocale().getLanguage());
		if (adminWelcomeMailingID <= 0 && !"en".equalsIgnoreCase(admin.getLocale().getLanguage())) {
			adminWelcomeMailingID = adminService.getAdminWelcomeMailingId("en");
		}
		if (adminWelcomeMailingID > 0) {
			final Preview preview = previewFactory.createPreview();
			final Page output = preview.makePreview(adminWelcomeMailingID, 0, true);
			preview.done();

			mailSubject = output.getHeaderField("subject").replace("{0}", admin.getUsername()).replace("{1}", admin.getUsername()).replace("{2}", admin.getFirstName()).replace("{3}", admin.getFullname());
			mailContentText = output.getText().replace("{0}", admin.getUsername()).replace("{1}", passwordResetLink).replace("{2}", admin.getFirstName()).replace("{3}", admin.getFullname());
			mailContentHtml = output.getHTML().replace("{0}", admin.getUsername()).replace("{1}", passwordResetLink).replace("{2}", admin.getFirstName()).replace("{3}", admin.getFullname());
		} else {
			mailSubject = I18nString.getLocaleString("user.welcome.mail.subject", locale, admin.getUsername(), passwordResetLink, admin.getFirstName(), admin.getFullname());
			mailContentText = I18nString.getLocaleString("user.welcome.mail.body.text", locale, admin.getUsername(), passwordResetLink, admin.getFirstName(), admin.getFullname());
			mailContentHtml = I18nString.getLocaleString("user.welcome.mail.body.html", locale, admin.getUsername(), passwordResetLink, admin.getFirstName(), admin.getFullname());
		}
		
		javaMailService.sendEmail(admin.getCompanyID(), admin.getEmail(), mailSubject, mailContentText, mailContentHtml);
		return new SimpleServiceResult(true);
	}
	
	@Override
	public boolean existsPasswordResetTokenHash(String username, String token) {
		return passwordResetDao.existsPasswordResetTokenHash(username, getTokenHash(token));
	}
	
	@Override
	public boolean isValidPasswordResetTokenHash(String username, String token) {
		return passwordResetDao.isValidPasswordResetTokenHash(username, getTokenHash(token));
	}

	@Override
	public void riseErrorCount(String username) {
		final Optional<Admin> adminOptional = adminService.getAdminByName(username);
		
		if (adminOptional.isPresent()) {
			passwordResetDao.riseErrorCount(adminOptional.get().getAdminID());
		}
	}

	@Override
	public void updateSessionsLanguagesAttributes(final Admin admin) {
		final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		attributes.setAttribute(Globals.LOCALE_KEY, admin.getLocale(), RequestAttributes.SCOPE_SESSION);  // To be removed when Struts message tags are not in use anymore.
		attributes.setAttribute("helplanguage", this.getHelpLanguage(admin), RequestAttributes.SCOPE_SESSION);
	}
}
