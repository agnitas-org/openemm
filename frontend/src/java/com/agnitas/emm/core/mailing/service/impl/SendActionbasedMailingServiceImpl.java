/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.util.Date;
import java.util.Hashtable;
import java.util.function.Supplier;

import org.agnitas.backend.Mailgun;
import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.TimeoutLRUMap;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.backend.MailgunFactory;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.ActionbasedMailingNotActivatedException;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingException;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;

/**
 * Implementation of {@link SendActionbasedMailingService} interface.
 */
public class SendActionbasedMailingServiceImpl implements SendActionbasedMailingService {
	/*
	 * TODO: Refactoring required!
	 *
	 * What is to do?
	 * - implement better error detection and exception handling for sending mailing
	 */

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(SendActionbasedMailingServiceImpl.class);

	private MaildropStatusDao maildropStatusDao;
	
	private ConfigService configService;

	/** Factory for instantiating new Mailgun objects. */
	private MailgunFactory mailgunFactory;

	/** Cache for Mailgun objects. */
	private TimeoutLRUMap<String, Mailgun> mailgunCache;

	@Override
	public final void sendActionbasedMailing(final int companyId, final int mailingId, final int customerId, final int delayMinutes, final MailgunOptions options) throws SendActionbasedMailingException {
		sendActionbasedMailing(new MailgunSupplier(companyId, mailingId), mailingId, customerId, delayMinutes, options);
	}

	private final void sendActionbasedMailing(final MailgunSupplier mailgunSupplier, final int mailingId, final int customerID, final int delayMinutes, final MailgunOptions options) throws SendActionbasedMailingException {
		try {
			final Mailgun mailgun = mailgunSupplier.get();
			final Hashtable<String, Object> opts = options == null ? new Hashtable<>() : options.asHashtable();

			opts.put("customer-id", Integer.toString(customerID));
			opts.put("send-date", DateUtils.addMinutes(new Date(), delayMinutes));

			mailgun.execute(opts);

		} catch(final Exception e) {
			logger.error("Cannot fire campaign-/event-mail", e);

			throw new UnableToSendActionbasedMailingException(mailingId, customerID, e);
		}
	}

	/**
	 * Set factory for {@link Mailgun}.
	 *
	 * @param factory factory for {@link Mailgun}
	 */
	@Required
	public void setMailgunFactory(final MailgunFactory factory) {
		this.mailgunFactory = factory;
	}

	@Required
	public void setMaildropStatusDao(final MaildropStatusDao maildropStatusDao) {
		this.maildropStatusDao = maildropStatusDao;
	}

	@Required
	public void setConfigService(final ConfigService configService) {
		this.configService = configService;
	}

	private class MailgunSupplier {
		private final int companyId;
		private final int mailingId;
		private final Supplier<MaildropEntry> maildropSupplier;

		public MailgunSupplier(final int companyId, final int mailingId) {
			this.companyId = companyId;
			this.mailingId = mailingId;
			this.maildropSupplier = () -> maildropStatusDao.getEntryForStatus(mailingId, companyId, MaildropStatus.ACTION_BASED.getCode());
		}

		/**
		 * Returns the Mailgun for given mailing. If there is no Mailgun cached for this mailing,
		 * a new Mailgun is created, configured and stored in cache.
		 *
		 * @return cached or new mailgun.
		 * @throws Exception on errors processing the method.
		 */
		public Mailgun get() throws Exception {
			final String cacheId = companyId + "_" + mailingId;
			Mailgun mailgun = getMailgunCache().get(cacheId);

			if (mailgun == null) {
				final MaildropEntry maildropEntry = getMaildropEntry();

				mailgun = mailgunFactory.newMailgun();
				if (mailgun == null) {
					logger.error("Mailgun could not be created: " + mailingId);
					throw new SendActionbasedMailingException("Unable to create Mailgun");
				}

				mailgun.initialize(Integer.toString(maildropEntry.getId()));
				mailgun.prepare(new Hashtable<>());

				getMailgunCache().put(cacheId, mailgun);
			}

			return mailgun;
		}

		private TimeoutLRUMap<String, Mailgun> getMailgunCache() {
			if (mailgunCache == null) {
				mailgunCache = new TimeoutLRUMap<>(configService.getIntegerValue(ConfigValue.MailgunMaxCache), configService.getLongValue(ConfigValue.MailgunMaxCacheTimeMillis));
			}
			
			return mailgunCache;
		}

		private MaildropEntry getMaildropEntry() throws Exception {
			final MaildropEntry entry = maildropSupplier.get();

			if (entry == null) {
				logger.warn("Event-mail for MailingID " + mailingId + " is not activated");
				throw new ActionbasedMailingNotActivatedException(mailingId);
			} else if (entry.getId() == 0) {
				throw new Exception("maildropStatusID is 0");
			}

			return entry;
		}
	}
}
