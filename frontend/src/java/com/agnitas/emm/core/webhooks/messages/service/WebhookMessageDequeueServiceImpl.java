/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.webhooks.config.WebhookConfigService;
import com.agnitas.emm.core.webhooks.messages.common.WebhookMessage;
import com.agnitas.emm.core.webhooks.messages.common.WebhookMessageStatus;
import com.agnitas.emm.core.webhooks.messages.dao.WebhookMessageQueueDao;

public final class WebhookMessageDequeueServiceImpl implements WebhookMessageDequeueService {
	
	private WebhookMessageQueueDao messageQueueDao;
	private WebhookConfigService configService;
	
	@Override
	public final List<WebhookMessage> listAndMarkMessagesForSending(final Date limitDate) {
		return messageQueueDao.listAndMarkMessagesForSending(limitDate);
	}

	@Override
	public final void markMessagesAsSent(final List<WebhookMessage> messages) {
		final ZonedDateTime now = ZonedDateTime.now();
		messageQueueDao.updateMessagesStatus(messages, now, WebhookMessageStatus.SENT, "Message sent");
	}

	@Override
	public final void markMessagesAsSendFailed(final List<WebhookMessage> messages, final String note) {
		for (final WebhookMessage message : messages) {
			final int maxRetryCount = this.configService.getMaximumRetryCount(message.getCompanyId());
			final int retryDelaySeconds = this.configService.getRetryDelaySeconds(message.getCompanyId());
			
			if (message.getRetryCount() < maxRetryCount - 1) {
				final ZonedDateTime now = ZonedDateTime.now().plusSeconds(retryDelaySeconds);

				messageQueueDao.updateMessageStatus(message.getEventId(), now, WebhookMessageStatus.IDLE, message.getRetryCount() + 1, note);
			} else {
				final ZonedDateTime now = ZonedDateTime.now();

				messageQueueDao.updateMessageStatus(message.getEventId(), now, WebhookMessageStatus.CANCELLED, message.getRetryCount() + 1, String.format("Max. retries reached (%s)", note));
			}
		}
	}

	@Override
	public final void cancelMessages(final List<WebhookMessage> messages, final String note) {
		final ZonedDateTime now = ZonedDateTime.now();
		messageQueueDao.updateMessagesStatus(messages, now, WebhookMessageStatus.CANCELLED, note);
	}
	
	@Override
	public final void cleanupMessages() {
		final ZonedDateTime now = ZonedDateTime.now();
		final ZonedDateTime before = now.minusSeconds(this.configService.getMessageRetentionSeconds());
		this.messageQueueDao.cleanupMessagesBefore(before);
	}

	public final void setWebhookMessageQueueDao(final WebhookMessageQueueDao dao) {
		this.messageQueueDao = Objects.requireNonNull(dao, "WebhookMessageQueueDao is null");
	}
	
	public final void setConfigService(final ConfigService service) {
		this.configService = new WebhookConfigService(Objects.requireNonNull(service, "ConfigService is null"));
	}
}
