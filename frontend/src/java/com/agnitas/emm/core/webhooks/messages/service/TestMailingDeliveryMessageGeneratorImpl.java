/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.service;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.messages.common.WebhookBackendData;

public class TestMailingDeliveryMessageGeneratorImpl extends AbstractWebhookBackendDataMessageGenerator {
	
	@Override
	void generateWebhookMessage(WebhookBackendData data) {
		messageEnqueueService.enqueueTestMailingDeliveredMessage(data.getCompanyID(), data.getMailingID(), data.getRecipientID(), data.getEventTimestamp());
	}

	@Override
	public WebhookEventType handledEventType() {
		return WebhookEventType.TEST_MAILING_DELIVERED;
	}

}
