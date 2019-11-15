/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.agnitas.beans.Company;
import org.agnitas.beans.EmmLayoutBase;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.i18n.LocaleContextHolder;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.dao.ComAdminDao;
import com.agnitas.dao.ComAdminPreferencesDao;
import com.agnitas.dao.ComCompanyDao;
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
	private static final transient Logger logger = Logger.getLogger( ComLogonServiceImpl.class);

	private static final transient String DEFAULT_HELP_LANGUAGE = "en";
	private static final int TOKEN_EXPIRATION_MINUTES = 30;

	private DataSource dataSource;
	
	private ConfigService configService;

	/** DAO for accessing EMM user data. */
	private ComAdminDao adminDao;

	private ComAdminPreferencesDao adminPreferencesDao;

	private ComEmmLayoutBaseDao emmLayoutBaseDao;
	
	/** DAO for accessing company data. */
	private ComCompanyDao companyDao;
	
	private AdminService adminService;

	private ComSupervisorService supervisorService;

	private ComPasswordCheck passwordCheck;

	private PasswordResetDao passwordResetDao;

	/** Service for login tracking. */
	private LoginTrackService loginTrackService;

	private JavaMailService javaMailService;

	private Set<String> supportedHtmlLanguages;

	// ----------------------------------------------------------- Business code
	
	@Override
	public ComAdmin getAdminByCredentials(String username, String password, String hostIpAddress) throws LogonServiceException {
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
	 * @return {@link ComAdmin} for credentials
	 * 
	 * @throws LogonServiceException on errors during login (invalid username/password, ...)
	 */
	private ComAdmin doRegularLogin( String username, String password, String hostIpAddress) throws LogonServiceException {
		ComAdmin admin = adminDao.getAdminByLogin( username, password);
		
		// Check, if we got an Admin (combination of username and password is valid)
		if (admin == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Login for user " + username + " failed - invalid combination of username and password");
			}
			loginTrackService.trackLoginFailed( hostIpAddress, username);

			throw new LogonFailedException(false);
		}
		
		// Check, if IP is currently blocked
		checkIPBlockState( admin.getCompanyID(), username, hostIpAddress);
		
			
		if (logger.isInfoEnabled()) {
			logger.info( "Login for user " + username + " successful");
		}
		
		loginTrackService.trackLoginSuccessful( hostIpAddress, username);

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
		Company company = companyDao.getCompany( companyID);
		if (loginTrackService.isIPLogonBlocked(hostIpAddress, company.getMaxLoginFails(), company.getLoginBlockTime())) {
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
	 * @return {@link ComAdmin} for credentials
	 * 
	 * @throws LogonServiceException on errors during login (username/password invalid, ...)
	 */
	private ComAdmin doSupervisorLogin(final String loginName, final String password, final String hostIpAddress) throws LogonServiceException {
		final String username = SupervisorUtil.getUserNameFromLoginName(loginName);
		final String supervisorName = SupervisorUtil.getSupervisorNameFromLoginName(loginName);
		
		// Get admin
		try {
			final ComAdmin admin = adminService.getAdminByNameForSupervisor(username, supervisorName, password);
			
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
	public ServiceResult<ComAdmin> authenticate(String username, String password, String clientIp) {
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
	public ComAdminPreferences getPreferences(ComAdmin admin) {
		return adminPreferencesDao.getAdminPreferences(admin.getAdminID());
	}

	@Override
	public EmmLayoutBase getEmmLayoutBase(ComAdmin admin) {
		return emmLayoutBaseDao.getEmmLayoutBase(admin.getCompanyID(), admin.getLayoutBaseID());
	}

	@Override
	public String getLayoutDirectory(String serverName) {
		return emmLayoutBaseDao.getLayoutDirectory(serverName);
	}

	@Override
	public String getHelpLanguage(ComAdmin admin) {
		if (admin == null) {
			return getHelpLanguage(LocaleContextHolder.getLocale().getLanguage());
		} else {
			return getHelpLanguage(admin.getAdminLang().trim().toLowerCase());
		}
	}

	@Override
	public PasswordState getPasswordState(ComAdmin admin) {
		if (admin.isSupervisor()) {
			return supervisorService.getPasswordState(admin.getSupervisor());
		} else {
			return adminService.getPasswordState(admin);
		}
	}

	@Override
	public Date getPasswordExpirationDate(ComAdmin admin) {
		if (admin.isSupervisor()) {
			return supervisorService.computePasswordExpireDate(admin.getSupervisor());
		} else {
			return adminService.computePasswordExpireDate(admin);
		}
	}

	@Override
	public SimpleServiceResult setPassword(ComAdmin admin, String password) {
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
		ComAdmin admin = getAdmin(username, email);

		if (admin == null) {
			loginTrackService.trackLoginFailed(clientIp, username);
			return new SimpleServiceResult(false, Message.of("error.passwordReset.auth"));
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
	public ServiceResult<ComAdmin> resetPassword(String username, String token, String password, String clientIp) {
		ComAdmin admin;

		try {
			admin = adminDao.getAdmin(username);
		} catch (AdminException e) {
			logger.error("Error occurred: " + e.getMessage(), e);
			loginTrackService.trackLoginFailed(clientIp, username);
			return new ServiceResult<>(null, false, Message.of("error.passwordReset.auth"));
		}

		if (passwordResetDao.existsPasswordResetTokenHash(username, getTokenHash(token))) {
			passwordResetDao.remove(admin.getAdminID());

			SimpleServiceResult result = setPassword(admin, password);

			if (result.isSuccess()) {
				return new ServiceResult<>(admin, true);
			} else {
				return new ServiceResult<>(null, false, result.getMessages());
			}
		} else {
			// If a password reset token is there increment errors count (to prevent brute force attack).
			// If a password reset token isn't there then a brute force attack is useless (there's no chance to hit).
			passwordResetDao.riseErrorCount(admin.getAdminID());
			return new ServiceResult<>(null, false, Message.of("error.passwordReset.auth"));
		}
	}

	private String getPasswordResetLink(String linkPattern, String username, String token) {
		try {
			String baseUrl = configService.getValue(AgnUtils.getHostName(), ConfigValue.SystemUrl);
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

	private ComAdmin getAdmin(String username, String email) {
		try {
			ComAdmin admin = adminDao.getAdmin(username);

			if (StringUtils.equalsIgnoreCase(admin.getEmail(), email)) {
				return admin;
			}
		} catch (AdminException e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		}

		return null;
	}

	private String generatePasswordResetToken(ComAdmin admin, String clientIp) {
		String token = SecurityTokenGenerator.generateSecurityToken();
		Date expirationDate = DateUtils.addMinutes(new Date(), TOKEN_EXPIRATION_MINUTES);

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

	private void sendPasswordResetMail(ComAdmin admin, String passwordResetLink) {
		Locale locale = admin.getLocale();

		String subject = I18nString.getLocaleString("passwordReset.mail.subject", locale, admin.getUsername());
		String textVersion = I18nString.getLocaleString("passwordReset.mail.body.text", locale, passwordResetLink);
		String htmlVersion = I18nString.getLocaleString("passwordReset.mail.body.html", locale, passwordResetLink);

		javaMailService.sendEmail(admin.getEmail(), subject, textVersion, htmlVersion);
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
		if (supportedHtmlLanguages.contains(preferredLanguage)) {
			return preferredLanguage;
		} else {
			return DEFAULT_HELP_LANGUAGE;
		}
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * Set DAO for accessing admin data.
	 *
	 * @param dao DAO for accessing admin data
	 */
	@Required
	public void setAdminDao( ComAdminDao dao) {
		this.adminDao = dao;
	}

	@Required
	public void setAdminPreferencesDao(ComAdminPreferencesDao dao) {
		this.adminPreferencesDao = dao;
	}

	@Required
	public void setEmmLayoutBaseDao(ComEmmLayoutBaseDao emmLayoutBaseDao) {
		this.emmLayoutBaseDao = emmLayoutBaseDao;
	}

	/**
	 * Set DAO for accessing company data.
	 *
	 * @param dao DAo for accessing company data
	 */
	@Required
	public void setCompanyDao( ComCompanyDao dao) {
		this.companyDao = dao;
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
	public void setSupportedHtmlLanguages(String[] supportedHtmlLanguages) {
		this.supportedHtmlLanguages = Stream.of(supportedHtmlLanguages)
				.map(StringUtils::trimToNull)
				.filter(Objects::nonNull)
				.map(String::toLowerCase)
				.collect(Collectors.toSet());
	}
}
