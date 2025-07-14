/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.registry.dao;

import java.util.List;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.registry.common.WebhookAlreadyRegisteredException;
import com.agnitas.emm.core.webhooks.registry.common.WebhookNotRegisteredException;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistryEntry;

/**
 * DAO to access registered webhook URLs.
 */
public interface WebhookRegistrationDao {

	/**
	 * Registers a new webhook URL.
	 * 
	 * @param companyId company ID
	 * @param eventType event type
	 * @param url webhook URL
	 * 
	 * @throws WebhookAlreadyRegisteredException if a webhook URL is already defined for company ID and event type
	 * 
	 * @throws NullPointerException if <code>url</code> or <code>eventType</code> is <code>null</code>
	 */
	public void registerWebhookUrl(final int companyId, final WebhookEventType eventType, final String url) throws WebhookAlreadyRegisteredException;
	
	/**
	 * Removed a webhook URL.
	 * 
	 * @param companyId company ID
	 * @param eventType event type
	 * 
	 * @throws WebhookNotRegisteredException if no webhook URL is defined for company ID and event type
	 * 
	 * @throws NullPointerException if <code>url</code> or <code>eventType</code> is <code>null</code>
	 */
	public void unregisterWebhookUrl(final int companyId, final WebhookEventType eventType) throws WebhookNotRegisteredException;
	
	/**
	 * Lists all registered webhook URLs for given event type.
	 * 
	 * @param eventType event type
	 * 
	 * @return list of registered webhook URLs
	 */
	public List<WebhookRegistryEntry> listAllRegisteredWebhookUrls(WebhookEventType eventType);

	/**
	 * Returns the webhook entry for given company ID and event type.
	 * 
	 * @param companyId company ID
	 * @param eventType event type
	 * 
	 * @return webhook entry for given company ID and event type
	 * 
	 * @throws WebhookNotRegisteredException if webhook is not registered for given company ID and event type
	 * 
	 * @throws NullPointerException if <code>url</code> or <code>eventType</code> is <code>null</code>
	 */
	public WebhookRegistryEntry findWebhookEntry(final int companyId, final WebhookEventType eventType) throws WebhookNotRegisteredException;
	
	/**
	 * Return true for successful deletion of company data
	 * @param companyId
	 * @return
	 */
	public boolean deleteWebhookUrlByCompany(final int companyId);
}
