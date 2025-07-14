/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.registry.common;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;

/**
 * Exception indicating that no webhook URL is defined
 * for a company ID and event type. 
 */
public final class WebhookNotRegisteredException extends WebhookRegistrationException {
	
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 * 
	 * @param companyId company ID
	 * @param eventType event type
	 */
	public WebhookNotRegisteredException(final int companyId, final WebhookEventType eventType) {
		super(companyId, eventType, String.format("Webhook URL not registered (company ID %d, webhook '%s')", companyId, eventType));
	}
}
