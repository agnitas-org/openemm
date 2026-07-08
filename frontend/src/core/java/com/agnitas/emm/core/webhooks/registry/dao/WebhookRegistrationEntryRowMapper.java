/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.registry.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistryEntry;

/**
 * {@link RowMapper} implementation for {@link WebhookRegistryEntry}.
 */
final class WebhookRegistrationEntryRowMapper implements RowMapper<WebhookRegistryEntry> {

	/** Pre-instantiated singleton instance. */
	public static final WebhookRegistrationEntryRowMapper INSTANCE = new WebhookRegistrationEntryRowMapper();
	
	@Override
	public final WebhookRegistryEntry mapRow(final ResultSet resultSet, final int row) throws SQLException {
		final int companyId = resultSet.getInt("company_ref");
		final int eventCode = resultSet.getInt("event_type");
		final String url = resultSet.getString("webhook_url");

		final Optional<WebhookEventType> eventTypeOpt = WebhookEventType.fromEventCode(eventCode);

		if(!eventTypeOpt.isPresent()) {
			throw new SQLException(String.format("Invalid webhook event type code: %d", eventCode));
		}
		
		return new WebhookRegistryEntry(companyId, eventTypeOpt.get(), url);
	}


}
