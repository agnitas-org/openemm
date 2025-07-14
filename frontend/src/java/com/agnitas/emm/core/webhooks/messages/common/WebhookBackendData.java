/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.common;

import java.time.ZonedDateTime;
import java.util.Objects;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;

public final class WebhookBackendData {

	private final long id;
	private final ZonedDateTime timestamp;
	private final ZonedDateTime eventTimestamp;
	private final WebhookEventType eventType;
	private final int companyID;
	private final int mailingID;
	private final int recipientID;

	public WebhookBackendData(final long id, final ZonedDateTime timestamp, final ZonedDateTime eventTimestamp, final WebhookEventType eventType, final int companyID, final int mailingID, final int recipientID) {
		this.id = id;
		this.timestamp = Objects.requireNonNull(timestamp, "Record timetamp is null");
		this.eventTimestamp = Objects.requireNonNull(eventTimestamp, "Event timestamp is null");
		this.eventType = Objects.requireNonNull(eventType, "Event type is null");
		this.companyID = companyID;
		this.mailingID = mailingID;
		this.recipientID = recipientID;
	}

	public final long getId() {
		return id;
	}

	public final ZonedDateTime getTimestamp() {
		return timestamp;
	}

	public final ZonedDateTime getEventTimestamp() {
		return eventTimestamp;
	}

	public final WebhookEventType getEventType() {
		return eventType;
	}

	public final int getCompanyID() {
		return companyID;
	}

	public final int getMailingID() {
		return mailingID;
	}

	public final int getRecipientID() {
		return recipientID;
	}

}
