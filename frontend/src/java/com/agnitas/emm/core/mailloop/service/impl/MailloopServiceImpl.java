/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailloop.service.impl;

import java.util.Map;

import com.agnitas.beans.Mailloop;
import com.agnitas.dao.MailloopDao;
import com.agnitas.emm.common.UserStatus;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingException;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mailloop.InvalidMailloopSecurityTokenException;
import com.agnitas.emm.core.mailloop.MailloopException;
import com.agnitas.emm.core.mailloop.UnableToSendAutoresponderMailingException;
import com.agnitas.emm.core.mailloop.UnknownMailloopIdException;
import com.agnitas.emm.core.mailloop.service.MailloopService;

/**
 * Implementation of {@link MailloopService}.
 */
public class MailloopServiceImpl implements MailloopService {

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(MailloopServiceImpl.class);

	/** DAO for accessing mailloop settings. */
	private MailloopDao mailloopDao;

	/** DAO for accessing mailing data. */
	private MailingDao mailingDao;

	/** Service for sending action-based mailings. */
	private SendActionbasedMailingService sendActionbasedMailingService;

	@Override
	public void sendAutoresponderMail(final int mailloopID, final int companyID, final int customerID, final String securityToken) throws MailloopException {
		if(logger.isInfoEnabled()) {
			logger.info(String.format("Called sendAutoresponderMail() - mailloop %d, company %d, customer %d", mailloopID, companyID, customerID));
		}

		final Mailloop mailloop = this.mailloopDao.getMailloop(mailloopID, companyID);

		if(mailloop == null || mailloop.getId() == 0) {
			if(logger.isInfoEnabled()) {
				logger.info(String.format("Unknown mailloop %d for company %d", mailloopID, companyID));
			}

			throw new UnknownMailloopIdException(mailloopID, companyID);
		}

		if(securityToken == null || !securityToken.equals(mailloop.getSecurityToken())) {
			throw new InvalidMailloopSecurityTokenException(mailloopID);
		}

		sendAutoresponderMail(mailloop, customerID);
	}

	/**
	 * Send auto-responder mail for previously loaded {@link Mailloop}.
	 *
	 * @param mailloop {@link Mailloop} for sending auto-responder
	 * @param customerID customer ID that triggered sending of the auto-responder
	 * @throws UnableToSendAutoresponderMailingException on errors sending auto-responder mailing
	 */
	private void sendAutoresponderMail(final Mailloop mailloop, final int customerID) throws UnableToSendAutoresponderMailingException {
		if(mailloop.getAutoresponderMailingId() == 0) {
			throw new UnableToSendAutoresponderMailingException(mailloop.getAutoresponderMailingId(), customerID, mailloop.getId());
		} else {
			sendAutoresponderAsActionbasedMailing(mailloop, customerID);
		}
	}

	/**
	 * Send auto-responder mail as action-based mailing for previously loaded {@link Mailloop}.
	 *
	 * @param mailloop {@link Mailloop} for sending auto-responder with {@link Mailloop#getAutoresponderMailingId()} returning a positive, non-zero value
	 * @param customerID customer ID that triggered sending of the auto-responder
	 *
	 * @throws UnableToSendAutoresponderMailingException on errors sending auto-responder mailing
	 */
	private void sendAutoresponderAsActionbasedMailing(final Mailloop mailloop, final int customerID) throws UnableToSendAutoresponderMailingException {
		final int autoresponderMailingId = mailloop.getAutoresponderMailingId();

		assert(autoresponderMailingId > 0);		// Satisfied due to previous checks

		final LightweightMailing mailing = mailingDao.getLightweightMailing(mailloop.getCompanyID(), autoresponderMailingId);

		final Map<String, String> overwriteMailgunOptions = null;				// Currently no mailgun option is overwritten

		try {
			final MailgunOptions mailgunOptions = new MailgunOptions();
			mailgunOptions.withForceSending(true);
			mailgunOptions.withAllowedUserStatus(UserStatus.Active, UserStatus.WaitForConfirm);
			mailgunOptions.withProfileFieldValues(overwriteMailgunOptions);
			mailgunOptions.withForceSending(true);
			sendActionbasedMailingService.sendActionbasedMailing(mailing.getCompanyID(), mailing.getMailingID(), customerID, 0, mailgunOptions);
		} catch(final SendActionbasedMailingException e) {
			logger.error("Error sending auto-responder", e);
			throw new UnableToSendAutoresponderMailingException(autoresponderMailingId, customerID, mailloop.getId(), e);
		}
	}

	/**
	 * Set DAO for accessing mailloop settings.
	 *
	 * @param dao  DAO for accessing mailloop settings
	 */
	public void setMailloopDao(final MailloopDao dao) {
		this.mailloopDao = dao;
	}

	/**
	 * Set service for sending action-based mailings.
	 *
	 * @param service service for sending action-based mailings
	 */
	public void setSendActionbasedMailingService(final SendActionbasedMailingService service) {
		this.sendActionbasedMailingService = service;
	}

	/**
	 * Set DAO for accessing mailing data.
	 *
	 * @param dao DAO for accessing mailing data
	 */
	public void setMailingDao(final MailingDao dao) {
		this.mailingDao = dao;
	}
}
