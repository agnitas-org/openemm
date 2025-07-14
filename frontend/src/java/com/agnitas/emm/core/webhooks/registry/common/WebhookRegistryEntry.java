/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.registry.common;

import java.util.Objects;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;

/**
 * Data from webhook URL registry.
 */
public final class WebhookRegistryEntry {

	/** Company ID. */
	private final int companyId;
	
	/** Event type. */ 
	private final WebhookEventType eventType;
	
	/** Configured webhook URL for {@link #companyId} and {@link #eventType}. */
	private final String url;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param companyId company ID
	 * @param eventType event type
	 * @param url configured URL for company ID and event type
	 * 
	 * @throws NullPointerException if <code>eventType</code> or <code>url</code> is <code>null</code>
	 */
	public WebhookRegistryEntry(final int companyId, final WebhookEventType eventType, final String url) {
		this.companyId = companyId;
		this.eventType = Objects.requireNonNull(eventType, "WebhookEventType is null");
		this.url = Objects.requireNonNull(url, "Webhook URL is null");
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
	 * Returns the event type.
	 * 
	 * @return event type.
	 */
	public final WebhookEventType getEventType() {
		return eventType;
	}

	/**
	 * Returns the webhook URL configured for company ID and event type.
	 * 
	 * @return configured webhook URL
	 */
	public final String getUrl() {
		return url;
	}
	
}
