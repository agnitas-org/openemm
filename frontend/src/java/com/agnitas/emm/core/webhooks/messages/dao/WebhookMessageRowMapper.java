/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.messages.common.WebhookMessage;

/**
 * RowMapper for {@link WebhookMessage}.
 */
final class WebhookMessageRowMapper implements RowMapper<WebhookMessage> {

	/** Singleton instance for use in DAO. */
	public static final WebhookMessageRowMapper INSTANCE = new WebhookMessageRowMapper();

	@Override
	public final WebhookMessage mapRow(final ResultSet resultSet, final int row) throws SQLException {
		final int companyID = resultSet.getInt("company_ref");
		final int eventTypeCode = resultSet.getInt("event_type");
		final Date timestamp = resultSet.getTimestamp("event_timestamp");
		final int retryCount = resultSet.getInt("retry_count");
		final long messageID = resultSet.getLong("message_id");
		final String payload = resultSet.getString("payload");
		
		final Optional<WebhookEventType> eventTypeOpt = WebhookEventType.fromEventCode(eventTypeCode);
		if(!eventTypeOpt.isPresent()) {
			throw new SQLException(String.format("Invalid event type code: %d", eventTypeCode));
		}
		
		final ZonedDateTime eventTimestamp = ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());

		return new WebhookMessage(companyID, eventTypeOpt.get(), eventTimestamp, retryCount, messageID, payload);
	}

}
