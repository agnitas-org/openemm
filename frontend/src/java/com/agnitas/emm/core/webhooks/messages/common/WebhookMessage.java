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

/**
 * Webhook message.
 */
public final class WebhookMessage {

	/** Company ID to which this message belongs. */
	private final int companyId;
	
	/** Type of event. */
	private final WebhookEventType eventType;
	
	/** Timestamp when event occured. */
	private final ZonedDateTime eventTimestamp;
	
	/** Current number of send retries. */
	private final int retryCount;
	
	/** ID of event. */
	private final long eventId;
	
	/** Event payload. */
	private final String payload;

	/**
	 * Creates a new instance.
	 * 
	 * @param companyId company ID
	 * @param eventType event type
	 * @param eventTimestamp event timestamp
	 * @param retryCount number of retries
	 * @param eventId event ID
	 * @param payload payload
	 * 
	 * @throws NullPointerException if <code>eventType</code>, <code>eventTimestamp</code> or <code>payload</code> is <code>null</code>
	 */
	public WebhookMessage(final int companyId, final WebhookEventType eventType, final ZonedDateTime eventTimestamp, final int retryCount, final long eventId, final String payload) {
		this.companyId = companyId;
		this.eventType = Objects.requireNonNull(eventType, "eventType is null");
		this.eventTimestamp = Objects.requireNonNull(eventTimestamp, "eventTimestamp is null");
		this.retryCount = retryCount;
		this.eventId = eventId;
		this.payload = Objects.requireNonNull(payload, "payload is null");
	}

	/**
	 * Returns the company ID.
	 *
	 * @return company ID
	 */
	public final int getCompanyId() {
		return companyId;
	}

	/**
	 * Gets the event type.
	 *
	 * @return the event type
	 */
	public final WebhookEventType getEventType() {
		return eventType;
	}

	/**
	 * Gets the event timestamp.
	 *
	 * @return the event timestamp
	 */
	public final ZonedDateTime getEventTimestamp() {
		return eventTimestamp;
	}

	/**
	 * Gets the retry count.
	 *
	 * @return the retry count
	 */
	public final int getRetryCount() {
		return retryCount;
	}

	/**
	 * Gets the event id.
	 *
	 * @return the event id
	 */
	public final long getEventId() {
		return eventId;
	}

	/**
	 * Gets the payload.
	 *
	 * @return the payload
	 */
	public final String getPayload() {
		return payload;
	}
	
}
