/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.settings.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.profilefields.service.WebhookProfileFieldsService;
import com.agnitas.emm.core.webhooks.registry.common.WebhookNotRegisteredException;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistrationException;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistryEntry;
import com.agnitas.emm.core.webhooks.registry.service.WebhookRegistrationService;
import com.agnitas.emm.core.webhooks.registry.service.WebhookUrlException;
import com.agnitas.emm.core.webhooks.settings.common.WebhookSettings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

public final class WebhookSettingsServiceImpl implements WebhookSettingsService {

	private WebhookRegistrationService registrationService;
	private WebhookProfileFieldsService profileFieldService;
	
	@Transactional
	@Override
	public final WebhookSettings findRegisteredWebhookSettings(final int companyID, final WebhookEventType eventType) throws WebhookRegistrationException {
		final WebhookRegistryEntry webhook = this.registrationService.findWebhookEntry(companyID, eventType);
		final Set<String> profileFields = this.profileFieldService.listProfileFieldNamesForWebhook(companyID, eventType);
		
		return new WebhookSettings(eventType, webhook.getUrl(), new ArrayList<>(profileFields));
	}

	@Transactional
	@Override
	public WebhookSettings findWebhookSettings(int companyID, WebhookEventType eventType) {
		final Set<String> profileFields = this.profileFieldService.listProfileFieldNamesForWebhook(companyID, eventType);

		try {
			final WebhookRegistryEntry webhook = this.registrationService.findWebhookEntry(companyID, eventType);
			
			return new WebhookSettings(eventType, webhook.getUrl(), new ArrayList<>(profileFields));
		} catch(final WebhookNotRegisteredException e) {
			return new WebhookSettings(eventType, "", new ArrayList<>(profileFields));
		}
	}

	@Transactional
	@Override
	public List<WebhookSettings> listWebhookSettings(int companyID) {
		final List<WebhookSettings> list = new ArrayList<>();
		
		for(final WebhookEventType eventType : WebhookEventType.values()) {
			list.add(findWebhookSettings(companyID, eventType));
		}

		return list;
	}

	@Transactional(rollbackFor = { WebhookRegistrationException.class, WebhookUrlException.class })
	@Override
	public final void updateWebhookSettings(final int companyID, final WebhookEventType eventType, final String url, final Set<String> profileFields) throws WebhookRegistrationException, WebhookUrlException {
		if(StringUtils.isBlank(url)) {
			this.registrationService.unregisterWebhookUrl(companyID, eventType);
		} else {
			this.registrationService.defineOrReplaceWebhookUrl(companyID, eventType, url);
		}
		
		this.profileFieldService.updateIncludedProfileField(companyID, eventType, profileFields);
	}
	
	public final void setWebhookRegistrationService(final WebhookRegistrationService service) {
		this.registrationService = Objects.requireNonNull(service, "WebhookRegistrationService is null");
	}
	
	public final void setWebhookProfileFieldsService(final WebhookProfileFieldsService service) {
		this.profileFieldService = Objects.requireNonNull(service, "WebhookProfileFieldsService is null");
	}

}
