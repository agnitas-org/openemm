/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.settings.service;

import java.util.List;
import java.util.Set;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistrationException;
import com.agnitas.emm.core.webhooks.registry.service.WebhookUrlException;
import com.agnitas.emm.core.webhooks.settings.common.WebhookSettings;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service interface for accessing webhook settings (url + profile fields);
 * 
 * <p>
 *   <p>Terminology:</p>
 *   <ul>
 *     <li><i>Registered webhook:</i> A webhook is considered as registered, if an URL is set for it.</li>
 *     <li><i>Non-registered webhook:</i> A webhook is not registered, if no URL is set for it. It is not registered even if profile fields are configured for it.</li>
 *   </ul>
 * </p>
 */
public interface WebhookSettingsService {

	/**
	 * Returns the settings for the registered webhook.
	 * 
	 * @param companyID company ID
	 * @param eventType event type
	 * 
	 * @return settings for registered webhook
	 * 
	 * @throws WebhookRegistrationException if webhook is not registered
	 */
	WebhookSettings findRegisteredWebhookSettings(final int companyID, final WebhookEventType eventType) throws WebhookRegistrationException;
	
	/**
	 * Returns the settings for the webook.
	 * If webhook is not registered, a settings object with no URL is returned.
	 * 
	 * @param companyID company ID
	 * @param eventType event type
	 * 
	 * @return settings for webhook
	 */
	WebhookSettings findWebhookSettings(int companyID, WebhookEventType eventType);
	
	/**
	 * Lists the settings for all webhooks of given company ID.
	 * 
	 * @param companyID company ID
	 * 
	 * @return list of all webhooks
	 */
	List<WebhookSettings> listWebhookSettings(final int companyID);

	/**
	 * Updates the settings for the given webhook.
	 * 
	 * Registers or updates given webhook, if URL is given. Unregisteres webhook, if given URL is empty or <code>null</code>.
	 * 
	 * If event type includes recipient data, profile field configuration is updated according to given list of profile fields in any case.
	 * 
	 * @param companyID company ID
	 * @param eventType event type
	 * @param url new URL
	 * @param profileFields profile fields included in webhook data
	 * 
	 * @throws WebhookRegistrationException on errors accessing webhook data
	 * @throws WebhookUrlException if webhook url is invalid
	 */
	@Transactional(rollbackFor = { WebhookRegistrationException.class, WebhookUrlException.class })
	void updateWebhookSettings(final int companyID, final WebhookEventType eventType, final String url, final Set<String> profileFields) throws WebhookRegistrationException, WebhookUrlException;
	
}
