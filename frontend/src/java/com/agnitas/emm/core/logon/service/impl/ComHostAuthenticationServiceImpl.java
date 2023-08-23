/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service.impl;

import java.util.Locale;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.util.HtmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.logon.dao.ComHostAuthenticationDao;
import com.agnitas.emm.core.logon.dao.HostAuthenticationDaoException;
import com.agnitas.emm.core.logon.dao.NoSecurityCodeHostAuthenticationDaoException;
import com.agnitas.emm.core.logon.service.ComHostAuthenticationService;
import com.agnitas.emm.core.logon.service.HostAuthenticationSecurityCodeGenerator;
import com.agnitas.emm.core.logon.service.HostAuthenticationServiceException;
import com.agnitas.emm.core.logon.web.CannotSendSecurityCodeException;
import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.service.SupervisorUtil;
import com.agnitas.messages.I18nString;

/**
 * Implementation of
 * {@link com.agnitas.emm.core.logon.service.ComHostAuthenticationService}.
 */
public class ComHostAuthenticationServiceImpl implements ComHostAuthenticationService {

	/** Validity period of authenticated hosts in days. */
	public static final transient int HOST_AUTHENTICATION_VALIDITY_PERIOD_DAYS = 90;

	/** Placeholder in mail for security code. */
	private static final transient String SECURITY_CODE_PLACEHOLDER = "${SECURITY_CODE}";

	/** Placeholder in mail for user name. */
	private static final transient String USERNAME_PLACEHOLDER = "${USERNAME}";

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComHostAuthenticationServiceImpl.class);

	@Override
	public boolean isHostAuthenticated(Admin admin, String hostId) throws HostAuthenticationServiceException {
		Supervisor supervisor = SupervisorUtil.extractSupervisor(admin);

		try {
			if (supervisor == null) {
				return this.hostAuthenticationDao.isHostAuthenticated(admin, hostId);
			} else {
				return this.hostAuthenticationDao.isHostAuthenticated(supervisor, hostId);
			}
		} catch (HostAuthenticationDaoException e) {
			String message = supervisor == null
					? "Error check host authentication for admin " + admin.getAdminID() + " on host " + hostId
					: "Error check host authentication for supervisor " + supervisor.getId() + " on host " + hostId;

			logger.error(message);

			throw new HostAuthenticationServiceException(message, e);
		}
	}

	@Override
	public boolean isHostAuthenticationEnabled(int companyID) {

		// Host authentication is enabled by default and will be disabled by special
		// configuration
		boolean enabled = this.configService.getBooleanValue(ConfigValue.HostAuthentication, companyID);

		if (logger.isInfoEnabled()) {
			if (enabled) {
				logger.info("Host authentication is ENABLED for company " + companyID);
			} else {
				logger.info("Host authentication is DISABLED for company " + companyID);
			}
		}

		return enabled;
	}

	@Override
	public void sendSecurityCode(Admin admin, String hostID) throws HostAuthenticationServiceException {
		// TODO: Method cannot be unit-tested. Final code for sending mail should be
		// moved behind interface.

		Supervisor supervisor = SupervisorUtil.extractSupervisor(admin);
		try {
			String securityCode = null;

			try {
				if (supervisor == null) {
					securityCode = this.hostAuthenticationDao.getSecurityCode(admin, hostID);

					if (logger.isInfoEnabled()) {
						logger.info("Found security code for admin " + admin.getAdminID() + " on host " + hostID);
					}
				} else {
					securityCode = this.hostAuthenticationDao.getSecurityCode(supervisor, hostID);

					if (logger.isInfoEnabled()) {
						logger.info("Found security code for supervisor " + admin.getAdminID() + " on host " + hostID);
					}
				}
			} catch (NoSecurityCodeHostAuthenticationDaoException e) {
				securityCode = this.securityCodeGenerator.createSecurityCode();

				if (supervisor == null) {
					if (logger.isInfoEnabled()) {
						logger.info("Found no security code for admin " + admin.getAdminID() + " on host " + hostID
								+ ". Creating new one.");
					}

					this.hostAuthenticationDao.writePendingSecurityCode(admin, hostID, securityCode);
				} else {
					if (logger.isInfoEnabled()) {
						logger.info("Found no security code for supervisor " + admin.getAdminID() + " on host " + hostID
								+ ". Creating new one.");
					}

					this.hostAuthenticationDao.writePendingSecurityCode(supervisor, hostID, securityCode);
				}
			}

			if (supervisor == null) {
				sendSecurityCodeByEmail(admin, securityCode); // TODO: This method call prevents unit-testing. Code of
																// this method should be move to own class
			} else {
				sendSecurityCodeByEmail(supervisor, admin, securityCode); // TODO: This method call prevents
																			// unit-testing. Code of this method should
																			// be move to own class
			}
		} catch (CannotSendSecurityCodeException e) {
			String msg = supervisor == null
					? "Error sending security code (admin " + admin.getAdminID() + ", host " + hostID + ")"
					: "Error sending security code (supervisor " + supervisor.getId() + ", host " + hostID + ")";

			logger.error(msg, e);

			throw e;
		} catch (Exception e) {
			String msg = supervisor == null
					? "Error sending security code (admin " + admin.getAdminID() + ", host " + hostID + ")"
					: "Error sending security code (supervisor " + supervisor.getId() + ", host " + hostID + ")";
			logger.error(msg, e);

			throw new CannotSendSecurityCodeException(msg, e);
		}

	}

	/**
	 * Send mail with security code.
	 * 
	 * @param admin
	 *            receiving admin
	 * @param securityCode
	 *            security code
	 *
	 * @throws CannotSendSecurityCodeException
	 *             on errors sending mail
	 */
	private void sendSecurityCodeByEmail(Admin admin, String securityCode) throws CannotSendSecurityCodeException {
		Locale locale = admin.getLocale();
		
		final String mailSubject;
		final String mailContentHtml;
		final String mailContentText;
		int securityCodeMailingID = adminService.getSecurityCodeMailingId(admin.getLocale().getLanguage());
		if (securityCodeMailingID <= 0 && !"en".equalsIgnoreCase(admin.getLocale().getLanguage())) {
			securityCodeMailingID = adminService.getSecurityCodeMailingId("en");
		}
		if (securityCodeMailingID > 0) {
			final Preview preview = previewFactory.createPreview();
			final Page output = preview.makePreview(securityCodeMailingID, 0, true);
			preview.done();

			mailSubject = output.getHeaderField("subject").replace("{0}", admin.getUsername()).replace("{1}", securityCode).replace("{2}", admin.getFirstName()).replace("{3}", admin.getFullname());
			mailContentText = output.getText().replace("{0}", admin.getUsername()).replace("{1}", securityCode).replace("{2}", admin.getFirstName()).replace("{3}", admin.getFullname());
			mailContentHtml = output.getHTML().replace("{0}", admin.getUsername()).replace("{1}", securityCode).replace("{2}", admin.getFirstName()).replace("{3}", admin.getFullname());
		} else {
			mailSubject = I18nString.getLocaleString("logon.hostauth.email.security_code.subject", admin.getLocale()).replace(USERNAME_PLACEHOLDER, admin.getUsername()).replace(SECURITY_CODE_PLACEHOLDER, securityCode).replace("\\n", "\n");
			mailContentText = I18nString.getLocaleString("logon.hostauth.email.security_code.content", admin.getLocale()).replace(USERNAME_PLACEHOLDER, admin.getUsername()).replace(SECURITY_CODE_PLACEHOLDER, securityCode).replace("\\n", "\n");
			mailContentHtml = HtmlUtils.replaceLineFeedsForHTML(mailContentText);
		}
		
		javaMailService.sendEmail(admin.getCompanyID(), admin.getEmail(), mailSubject, mailContentText, mailContentHtml);
	}

	/**
	 * Send mail with security code.
	 * 
	 * @param supervisor
	 *            receiver
	 * @param admin
	 *            admin used at supervisor login
	 * @param securityCode
	 *            security code
	 *
	 * @throws CannotSendSecurityCodeException
	 *             on errors sending mail
	 */
	private void sendSecurityCodeByEmail(Supervisor supervisor, Admin admin, String securityCode) throws CannotSendSecurityCodeException {
		String subjectTemplate = I18nString.getLocaleString("logon.hostauth.email.security_code.subject_supervisor", admin.getLocale());
		String messageTemplate = I18nString.getLocaleString("logon.hostauth.email.security_code.content_supervisor", admin.getLocale());

		String subject = subjectTemplate.replace(USERNAME_PLACEHOLDER, admin.getUsername()).replace(SECURITY_CODE_PLACEHOLDER, securityCode).replace("\\n", "\n");
		String message = messageTemplate.replace(USERNAME_PLACEHOLDER, admin.getUsername()).replace(SECURITY_CODE_PLACEHOLDER, securityCode).replace("\\n", "\n");

		try {
			boolean result = javaMailService.sendEmail(admin.getCompanyID(), supervisor.getEmail(), subject, message, HtmlUtils.replaceLineFeedsForHTML(message));

			if (!result) {
				logger.error("Unable to send email with security code?");

				throw new CannotSendSecurityCodeException("Error sending mail with security code");
			}
		} catch (Exception e) {
			logger.error("Error sending email with security code", e);

			throw new CannotSendSecurityCodeException(supervisor.getEmail(), e);
		}
	}

	@Override
	public void writeHostAuthentication(Admin admin, String hostID) throws HostAuthenticationServiceException {
		Supervisor supervisor = SupervisorUtil.extractSupervisor(admin);

		try {
			if (supervisor == null) {
				this.hostAuthenticationDao.writeHostAuthentication(admin, hostID, HOST_AUTHENTICATION_VALIDITY_PERIOD_DAYS);
				this.hostAuthenticationDao.removePendingSecurityCode(admin, hostID);
			} else {
				this.hostAuthenticationDao.writeHostAuthentication(supervisor, hostID, HOST_AUTHENTICATION_VALIDITY_PERIOD_DAYS);
				this.hostAuthenticationDao.removePendingSecurityCode(supervisor, hostID);
			}
		} catch (HostAuthenticationDaoException e) {
			String msg = supervisor == null
					? "Error writing host authentication data for admin " + admin.getAdminID() + " on host " + hostID
					: "Error writing host authentication data for supervisor " + supervisor.getId() + " on host " + hostID;

			logger.error(msg, e);

			throw new HostAuthenticationServiceException(msg, e);
		}
	}

	@Override
	public String getPendingSecurityCode(Admin admin, String hostID) throws HostAuthenticationServiceException {
		Supervisor supervisor = SupervisorUtil.extractSupervisor(admin);

		try {
			if (supervisor == null) {
				return this.hostAuthenticationDao.getSecurityCode(admin, hostID);
			} else {
				return this.hostAuthenticationDao.getSecurityCode(supervisor, hostID);
			}
		} catch (NoSecurityCodeHostAuthenticationDaoException e) {
			String msg = supervisor == null
					? "No pending security code found for admin " + admin.getAdminID() + " on host " + hostID
					: "No pending security code found for supervisor " + supervisor.getId() + " on host " + hostID;
			logger.warn(msg, e);

			throw new HostAuthenticationServiceException(msg, e);
		} catch (HostAuthenticationDaoException e) {
			String msg = supervisor == null
					? "Error reading pending security code for admin " + admin.getAdminID() + " on host " + hostID
					: "Error reading pending security code for supervisor " + supervisor.getId() + " on host " + hostID;
			logger.error(msg, e);

			throw new HostAuthenticationServiceException(msg, e);
		}
	}

	@Override
	public void removeAllExpiredData() {
		if (logger.isInfoEnabled()) {
			logger.info("Removing expired data for host authentications (pending security codes and host authentications).");
		}

		this.hostAuthenticationDao.removeExpiredHostAuthentications();
		this.hostAuthenticationDao.removeExpiredPendingsAuthentications(configService.getMaxPendingHostAuthenticationsAgeMinutes());
	}


	@Override
	public void removeAuthentictedHost(final String hostId) {
		this.hostAuthenticationDao.removeAuthentictedHost(hostId);
	}

	// ----------------------------------------------------------------------------------------------
	// Dependency Injection
	/** Service for accessing database-based configuration. */
	private ConfigService configService;

	/** Generator for security codes. */
	private HostAuthenticationSecurityCodeGenerator securityCodeGenerator;

	/** DAO for accessing host authentication data. */
	private ComHostAuthenticationDao hostAuthenticationDao;

	private JavaMailService javaMailService;
	
	protected AdminService adminService;
	
	private PreviewFactory previewFactory;

	/**
	 * Set service for accessing database-based configuration.
	 * 
	 * @param service
	 *            service for accessing DB-based configuration.
	 */
	@Required
	public void setConfigService(ConfigService service) {
		this.configService = service;
	}

	/**
	 * Set DAO for accessing host authentication data.
	 * 
	 * @param dao
	 *            DAO for host authentication data
	 */
	@Required
	public void setHostAuthenticationDao(ComHostAuthenticationDao dao) {
		this.hostAuthenticationDao = dao;
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
	public void setPreviewFactory(final PreviewFactory previewFactory) {
		this.previewFactory = previewFactory;
	}

	/**
	 * Set generator for security codes.
	 * 
	 * @param generator
	 *            generator for security codes
	 */
	@Required
	public void setSecurityCodeGenerator(HostAuthenticationSecurityCodeGenerator generator) {
		this.securityCodeGenerator = generator;
	}
}
