/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.dao;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.messages.common.WebhookMessage;
import com.agnitas.emm.core.webhooks.messages.common.WebhookMessageStatus;

import org.json.JSONObject;

public interface WebhookMessageQueueDao {

	List<WebhookMessage> listAndMarkMessagesForSending(final Date limitDate);

	void enqueueMessage(int companyId, WebhookEventType eventType, ZonedDateTime eventTimestamp, JSONObject payload);

	void updateMessagesStatus(final List<WebhookMessage> messages, final ZonedDateTime newSendTimestamp, final WebhookMessageStatus newStatus, final String statusNote);
	
	void updateMessageStatus(final long messageId, final ZonedDateTime newSendTimestamp, final WebhookMessageStatus newStatus, final int newRetryCount, final String statusNote);

	void cleanupMessagesBefore(final ZonedDateTime before);
	
	boolean cleanupMessagesByCompany(final int companyId);
}
