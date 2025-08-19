/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.service;

import java.time.ZonedDateTime;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.messages.common.WebhookBackendData;

public final class ProfileFieldChangedMessageGeneratorImpl extends AbstractWebhookBackendDataMessageGenerator {

	@Override
	public void generateWebhookMessages(int companyId) {
		processTimestampDao.findTimestampOfLastRun(companyId, handledEventType())
			.ifPresent(lastRun -> messageEnqueueService.enqueueProfileFieldChangedMessages(companyId, lastRun));

		int messageGenerationGraceSeconds = configService.getMessageGenerationGracePeriodSeconds(companyId);
		ZonedDateTime now = ZonedDateTime.now().minusSeconds(messageGenerationGraceSeconds);
		this.processTimestampDao.updateTimestampOfLastRun(companyId, handledEventType(), now);
	}

	@Override
	void generateWebhookMessage(WebhookBackendData data) {
		// not a backand depended message type
	}

	@Override
	public WebhookEventType handledEventType() {
		return WebhookEventType.PROFILE_FIELD_CHANGED;
	}
}
