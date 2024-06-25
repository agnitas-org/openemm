/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service.impl;

import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import org.agnitas.emm.core.recipient.service.SubscriberLimitExceededException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.dao.ComRecipientDao;

public class SubscriberLimitCheckImpl implements SubscriberLimitCheck {
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(SubscriberLimitCheckImpl.class);

	private final ConfigService configService;
	private final ComRecipientDao recipientDao;

	public SubscriberLimitCheckImpl(final ConfigService configService, final ComRecipientDao recipientDao) {
		this.configService = Objects.requireNonNull(configService, "configService");
		this.recipientDao = Objects.requireNonNull(recipientDao, "recipientDao");
	}

	@Override
	public SubscriberLimitCheckResult checkSubscriberLimit(final int companyId) throws SubscriberLimitExceededException {
		return checkSubscriberLimit(companyId, 1);
	}

	@Override
	public SubscriberLimitCheckResult checkSubscriberLimit(final int companyId, final int numNewSubscribers) throws SubscriberLimitExceededException {
		final int maximumSubscribers = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfCustomers, companyId);
		final int gracefulExtension = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfCustomers_Graceful, companyId);

		final int subscriberCount = recipientDao.getNumberOfRecipients(companyId);
		
		if (maximumSubscribers < 0) {
			return new SubscriberLimitCheckResult(subscriberCount, maximumSubscribers, gracefulExtension, false);
		} else {
			if (subscriberCount + numNewSubscribers <= maximumSubscribers) {
				return new SubscriberLimitCheckResult(subscriberCount, maximumSubscribers, gracefulExtension, false);
			} else if (subscriberCount + numNewSubscribers <= maximumSubscribers + gracefulExtension) {
				return new SubscriberLimitCheckResult(subscriberCount, maximumSubscribers, gracefulExtension, true);
			} else {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(String.format("Subscriber limit for company %d exceeded (current: %d, new: %d, allowed: %d)", companyId, subscriberCount, numNewSubscribers, maximumSubscribers));
				}

				throw new SubscriberLimitExceededException(maximumSubscribers, subscriberCount + numNewSubscribers);
			}
		}
	}
}
