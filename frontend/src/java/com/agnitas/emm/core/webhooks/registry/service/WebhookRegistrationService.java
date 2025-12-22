/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.registry.service;

import java.util.List;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.registry.common.WebhookNotRegisteredException;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistrationException;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistryEntry;

/**
 * Registration service for webhook URLs.
 */
public interface WebhookRegistrationService {

	/**
	 * Defines or replaces a webhook URL for given company ID and event type.
	 * 
	 * @param companyId company ID
	 * @param eventType event type
	 * @param url webhook URL
	 * 
	 * @throws WebhookRegistrationException on errors settings webhook URL
	 * @throws WebhookUrlException on invalid webhook URL
	 * @throws NullPointerException if <code>eventType</code> or <code>url</code> is <code>null</code>
	 */
	public void defineOrReplaceWebhookUrl(final int companyId, final WebhookEventType eventType, final String url) throws WebhookRegistrationException, WebhookUrlException;
	
	/**
	 * Removes a webhook URL from given company ID and event type.
	 * If no URL is defined, nothing happens.
	 * 
	 * @param companyId company ID
	 * @param eventType event type
	 * 
	 * @throws NullPointerException if <code>eventType</code> is <code>null</code>
	 */
	public void unregisterWebhookUrl(final int companyId, final WebhookEventType eventType);
	
	public List<WebhookRegistryEntry> listAllRegisteredWebhookUrls(final WebhookEventType eventType);
	
	/**
	 * Returns the webhook URL configuration for given company ID and event type.
	 * 
	 * @param companyId company ID
	 * @param eventType event type
	 * 
	 * @return webhook URL configuration
	 * 
	 * @throws WebhookNotRegisteredException if no webhook URL is configured
	 */
	public WebhookRegistryEntry findWebhookEntry(final int companyId, final WebhookEventType eventType) throws WebhookNotRegisteredException;
	
	/**
	 * Validates webhook url (structural validation).
	 * Does not validate that URL is reachable.
	 * 
	 * @param url url to validate
	 * 
	 * @throws WebhookUrlException on validation errors
	 */
	public void validateWebhookUrl(final String url) throws WebhookUrlException;
}
