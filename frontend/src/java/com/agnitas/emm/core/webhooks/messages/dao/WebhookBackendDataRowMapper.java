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
import com.agnitas.emm.core.webhooks.messages.common.WebhookBackendData;

final class WebhookBackendDataRowMapper implements RowMapper<WebhookBackendData> {

	/** Singleton instance for use in DAO layer. */
	public static final WebhookBackendDataRowMapper INSTANCE = new WebhookBackendDataRowMapper();
	
	@Override
	public final WebhookBackendData mapRow(final ResultSet resultSet, final int row) throws SQLException {
		final long id = resultSet.getLong("id");
		final Date recordTimestamp = resultSet.getTimestamp("creation_date");
		final Date eventTimestamp = resultSet.getTimestamp("event_timestamp");
		final int eventTypeCode = resultSet.getInt("event_type");
		final int companyID = resultSet.getInt("company_id");
		final int mailingID = resultSet.getInt("mailing_id");
		final int recipientID = resultSet.getInt("recipient_id");

		
		final ZonedDateTime recordDateTime = ZonedDateTime.ofInstant(recordTimestamp.toInstant(), ZoneId.systemDefault());
		final ZonedDateTime eventDateTime = ZonedDateTime.ofInstant(eventTimestamp.toInstant(), ZoneId.systemDefault());

		final Optional<WebhookEventType> eventTypeOpt = WebhookEventType.fromEventCode(eventTypeCode);
		if(!eventTypeOpt.isPresent()) {
			throw new SQLException(String.format("Invalid event type code: %d", eventTypeCode));
		}

		return new WebhookBackendData(id, recordDateTime, eventDateTime, eventTypeOpt.get(), companyID, mailingID, recipientID);
	}

}
