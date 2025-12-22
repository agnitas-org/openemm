/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.dao;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.messages.common.WebhookMessage;
import com.agnitas.emm.core.webhooks.messages.common.WebhookMessageStatus;
import org.json.JSONObject;

public class WebhookMessageQueueDaoImpl extends BaseDaoImpl implements WebhookMessageQueueDao {

	private ConfigService configService;

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Override
	public void enqueueMessage(int companyId, WebhookEventType eventType, ZonedDateTime eventTimestamp, JSONObject payload) {
		final String sql = isOracleDB()
				? "INSERT INTO webhook_message_tbl (message_id, company_ref, event_type, event_timestamp, send_timestamp, retry_count, payload, status, status_note) VALUES (webhook_message_seq.NEXTVAL, ?, ?, ?, ?, 0, ?, ?, 'Message enqueued')"
				: "INSERT INTO webhook_message_tbl (company_ref, event_type, event_timestamp, send_timestamp, retry_count, payload, status, status_note) VALUES (?, ?, ?, ?, 0, ?, ?, 'Message enqueued')";

		final Date eventDate = Date.from(eventTimestamp.toInstant());

		update(sql, companyId, eventType.getEventCode(), eventDate, eventDate, payload.toString(), WebhookMessageStatus.IDLE.getStatusCode());
	}

	@Override
	public List<WebhookMessage> listAndMarkMessagesForSending(Date limitDate) {
		// Get oldest entry for delivery
        List<Map<String, Object>> result = select(addRowLimit("""
				SELECT company_ref, event_type
				FROM webhook_message_tbl
				WHERE status = ?
				  AND send_timestamp <= ?
				ORDER BY send_timestamp ASC
				""", 1), WebhookMessageStatus.IDLE.getStatusCode(), limitDate);

		if (result.isEmpty()) {
			return Collections.emptyList();
		}

		int nextCompanyIdToSend = ((Number) result.get(0).get("company_ref")).intValue();
		int nextEventTypeToSend = ((Number) result.get(0).get("event_type")).intValue();

		int messagePackageSizeLimit = configService.getIntegerValue(ConfigValue.Webhooks.WebhooksMessagePacketSizeLimit, nextCompanyIdToSend);

		List<WebhookMessage> messages = select(
				addRowLimit("""
						SELECT *
						FROM webhook_message_tbl
						WHERE status = ?
						  AND company_ref = ?
						  AND event_type = ?
						  AND send_timestamp <= CURRENT_TIMESTAMP
						ORDER BY send_timestamp ASC
						""", messagePackageSizeLimit),
				WebhookMessageRowMapper.INSTANCE,
				WebhookMessageStatus.IDLE.getStatusCode(),
				nextCompanyIdToSend,
				nextEventTypeToSend
		);

		markAsSending(messages);

		return messages;
	}

	private void markAsSending(List<WebhookMessage> messages) {
		if (messages.isEmpty()) {
			return;
		}

		List<Object[]> parameterList = new ArrayList<>();
		for (WebhookMessage message : messages) {
			parameterList.add(new Object[] { WebhookMessageStatus.SENDING.getStatusCode(), message.getEventId() });
		}
		batchupdate("UPDATE webhook_message_tbl SET status = ?, status_note = 'Sending' WHERE message_id = ?", parameterList);
	}

	@Override
	public void updateMessagesStatus(List<WebhookMessage> messages, ZonedDateTime newSendDate, WebhookMessageStatus newStatus, String note) {
		if (!messages.isEmpty()) {
			final Date timestamp = Date.from(newSendDate.toInstant());

			List<Object[]> parameterList = new ArrayList<>();
			for (WebhookMessage message : messages) {
				parameterList.add(new Object[] { newStatus.getStatusCode(), note, timestamp, message.getEventId() });
			}
            batchupdate("UPDATE webhook_message_tbl SET status = ?, status_note = ?, send_timestamp = ? WHERE message_id = ?", parameterList);
		}
	}

	@Override
	public void updateMessageStatus(long messageId, ZonedDateTime newSendDate, WebhookMessageStatus newStatus, int newRetryCount, String note) {
		final String sql = "UPDATE webhook_message_tbl SET status = ?, retry_count = ?, status_note = ?, send_timestamp = ? WHERE message_id = ?";

		final Date timestamp = Date.from(newSendDate.toInstant());

		update(sql, newStatus.getStatusCode(), newRetryCount, note, timestamp, messageId);
	}

	@Override
	public void cleanupMessagesBefore(ZonedDateTime before) {
		final Date beforeDate = Date.from(before.toInstant());

		final String sql = "DELETE FROM webhook_message_tbl WHERE (status = ? OR status = ?) AND send_timestamp < ?";

		update(sql, WebhookMessageStatus.SENT.getStatusCode(), WebhookMessageStatus.CANCELLED.getStatusCode(), beforeDate);
	}

	@Override
	public boolean cleanupMessagesByCompany(int companyId) {
		int touchedLines = update("DELETE FROM webhook_message_tbl WHERE company_ref = ?", companyId);
    	if (touchedLines > 0) {
    		return true;
    	}

		int remaining = selectInt("SELECT COUNT(*) FROM webhook_message_tbl WHERE company_ref = ?", companyId);
		return remaining == 0;
	}

}
