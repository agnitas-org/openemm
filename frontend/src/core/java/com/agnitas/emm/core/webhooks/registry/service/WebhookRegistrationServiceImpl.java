/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.registry.service;

import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.registry.common.WebhookNotRegisteredException;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistryEntry;
import com.agnitas.emm.core.webhooks.registry.dao.WebhookRegistrationDao;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link WebhookRegistrationService} interface.
 */
public class WebhookRegistrationServiceImpl implements WebhookRegistrationService {

	/** DAO for registering webhook URLs. */
	private WebhookRegistrationDao webhookRegistrationDao;
	
	/** Validator for webhook urls. */
	private static final WebhookUrlValidator URL_VALIDATOR = new WebhookUrlValidator();
	
	@Transactional
	@Override
	public void defineOrReplaceWebhookUrl(int companyId, WebhookEventType eventType, String url) {
		validateWebhookUrl(url);
		
		this.unregisterWebhookUrl(companyId, eventType);
		this.webhookRegistrationDao.registerWebhookUrl(companyId, eventType, url);
		
		// TODO Update internal cache
	}

	@Override
	public void unregisterWebhookUrl(int companyId, WebhookEventType eventType) {
		try {
			this.webhookRegistrationDao.unregisterWebhookUrl(companyId, eventType);
		} catch(WebhookNotRegisteredException e) {
			// Do nothing
		}

		// TODO Update internal cache
	}

	@Override
	public List<WebhookRegistryEntry> listAllRegisteredWebhookUrls(WebhookEventType eventType) {
		return this.webhookRegistrationDao.listAllRegisteredWebhookUrls(eventType);
		// TODO Update internal cache according to read data
	}
	

	@Override
	public WebhookRegistryEntry findWebhookEntry(int companyId, WebhookEventType eventType) {
		// TODO Use internal cache
		return this.webhookRegistrationDao.findWebhookEntry(companyId, eventType);
	}
	
	@Override
	public void validateWebhookUrl(String url) {
		URL_VALIDATOR.validateWebhookUrl(url);
	}
	
	/**
	 * Set DAO for registering webhook URLs.
	 * 
	 * @param dao DAO for registering webhook URLs
	 */
	public void setWebhookRegistrationDao(WebhookRegistrationDao dao) {
		this.webhookRegistrationDao = Objects.requireNonNull(dao, "WebhookRegistrationDao is null");
	}

}
