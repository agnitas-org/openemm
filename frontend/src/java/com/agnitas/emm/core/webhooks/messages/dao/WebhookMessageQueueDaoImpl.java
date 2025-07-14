/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.dao;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.messages.common.WebhookMessage;
import com.agnitas.emm.core.webhooks.messages.common.WebhookMessageStatus;
import org.json.JSONObject;
import com.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Inplementation of {@link WebhookMessageQueueDao}.
 */
public final class WebhookMessageQueueDaoImpl extends BaseDaoImpl implements WebhookMessageQueueDao {

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
	public final List<WebhookMessage> listAndMarkMessagesForSending(final Date limitDate) {
		// Get oldest entry for delivery
		int nextCompanyIdToSend = 0;
		int nextEventTypeToSend = 0;
		if (isOracleDB()) {
			List<Map<String,Object>> result = select("SELECT company_ref, event_type FROM (SELECT company_ref, event_type FROM webhook_message_tbl WHERE status = ? AND send_timestamp <= ? ORDER BY send_timestamp ASC) WHERE rownum <= 1", WebhookMessageStatus.IDLE.getStatusCode(), limitDate);
			if (result.size() > 0) {
				nextCompanyIdToSend = ((Number) result.get(0).get("company_ref")).intValue();
				nextEventTypeToSend = ((Number) result.get(0).get("event_type")).intValue();
			}
		} else {
			List<Map<String,Object>> result = select("SELECT company_ref, event_type FROM webhook_message_tbl WHERE status = ? AND send_timestamp <= ? ORDER BY send_timestamp ASC LIMIT 1", WebhookMessageStatus.IDLE.getStatusCode(), limitDate);
			if (result.size() > 0) {
				nextCompanyIdToSend = ((Number) result.get(0).get("company_ref")).intValue();
				nextEventTypeToSend = ((Number) result.get(0).get("event_type")).intValue();
			}
		}
		
		if (nextCompanyIdToSend > 0) {
			int messagePackageSizeLimit = configService.getIntegerValue(ConfigValue.Webhooks.WebhooksMessagePacketSizeLimit, nextCompanyIdToSend);
			// Select all messages that can be sent for this companyid
			final List<WebhookMessage> messages;
			if (isOracleDB()) {
				messages = select("SELECT * FROM (SELECT * FROM webhook_message_tbl WHERE status = ? AND company_ref = ? AND event_type = ? AND send_timestamp <= CURRENT_TIMESTAMP ORDER BY send_timestamp ASC) WHERE rownum <= " + messagePackageSizeLimit, WebhookMessageRowMapper.INSTANCE, WebhookMessageStatus.IDLE.getStatusCode(), nextCompanyIdToSend, nextEventTypeToSend);
			} else {
				messages = select("SELECT * FROM webhook_message_tbl WHERE status = ? AND company_ref = ? AND event_type = ? AND send_timestamp <= CURRENT_TIMESTAMP ORDER BY send_timestamp ASC LIMIT " + messagePackageSizeLimit, WebhookMessageRowMapper.INSTANCE, WebhookMessageStatus.IDLE.getStatusCode(), nextCompanyIdToSend, nextEventTypeToSend);
			}
			
			if (!messages.isEmpty()) {
				// Mark all messages as "sending"
				List<Object[]> parameterList = new ArrayList<>();
				for (final WebhookMessage message : messages) {
					parameterList.add(new Object[] { WebhookMessageStatus.SENDING.getStatusCode(), message.getEventId() });
				}
	            batchupdate("UPDATE webhook_message_tbl SET status = ?, status_note = 'Sending' WHERE message_id = ?", parameterList);
			}
			
			return messages;
		} else {
			return null;
		}
	}

	@Override
	public final void updateMessagesStatus(final List<WebhookMessage> messages, final ZonedDateTime newSendDate, final WebhookMessageStatus newStatus, final String note) {
		if (!messages.isEmpty()) {
			final Date timestamp = Date.from(newSendDate.toInstant());

			List<Object[]> parameterList = new ArrayList<>();
			for (final WebhookMessage message : messages) {
				parameterList.add(new Object[] { newStatus.getStatusCode(), note, timestamp, message.getEventId() });
			}
            batchupdate("UPDATE webhook_message_tbl SET status = ?, status_note = ?, send_timestamp = ? WHERE message_id = ?", parameterList);
		}
	}

	@Override
	public final void updateMessageStatus(final long messageId, final ZonedDateTime newSendDate, final WebhookMessageStatus newStatus, final int newRetryCount, final String note) {
		final String sql = "UPDATE webhook_message_tbl SET status = ?, retry_count = ?, status_note = ?, send_timestamp = ? WHERE message_id = ?";
		
		final Date timestamp = Date.from(newSendDate.toInstant());
		
		update(sql, newStatus.getStatusCode(), newRetryCount, note, timestamp, messageId);
	}

	@Override
	public void cleanupMessagesBefore(final ZonedDateTime before) {
		final Date beforeDate = Date.from(before.toInstant());
		
		final String sql = "DELETE FROM webhook_message_tbl WHERE (status = ? OR STATUS = ?) AND send_timestamp < ?";
		
		update(sql, WebhookMessageStatus.SENT.getStatusCode(), WebhookMessageStatus.CANCELLED.getStatusCode(), beforeDate);
	}
	@Override
	public boolean cleanupMessagesByCompany(final int companyId) {
		int touchedLines = update("DELETE FROM webhook_message_tbl WHERE company_ref = ?", companyId);
    	if (touchedLines > 0) {
    		return true;
    	} else {
    		int remaining = selectInt("SELECT COUNT(*) FROM webhook_message_tbl WHERE company_ref = ?", companyId);
    		return remaining == 0;
    	}
	}
}
