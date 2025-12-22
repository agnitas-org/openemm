/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.service.impl;

import java.util.Objects;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import com.agnitas.emm.core.recipient.exception.SubscriberLimitExceededException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.dao.RecipientDao;

public class SubscriberLimitCheckImpl implements SubscriberLimitCheck {

	private static final Logger LOGGER = LogManager.getLogger(SubscriberLimitCheckImpl.class);

	private final ConfigService configService;
	private final RecipientDao recipientDao;

	public SubscriberLimitCheckImpl(final ConfigService configService, final RecipientDao recipientDao) {
		this.configService = Objects.requireNonNull(configService, "configService");
		this.recipientDao = Objects.requireNonNull(recipientDao, "recipientDao");
	}

	@Override
	public SubscriberLimitCheckResult checkSubscriberLimit(int companyId) {
		return checkSubscriberLimit(companyId, 1);
	}

	@Override
	public SubscriberLimitCheckResult checkSubscriberLimit(int companyId, int numNewSubscribers) {
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
