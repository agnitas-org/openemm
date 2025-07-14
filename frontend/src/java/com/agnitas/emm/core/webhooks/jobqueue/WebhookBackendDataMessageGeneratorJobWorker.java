/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.jobqueue;

import java.util.List;

import com.agnitas.service.JobWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.messages.service.WebhookBackendDataMessageGeneratorService;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistryEntry;
import com.agnitas.emm.core.webhooks.registry.service.WebhookRegistrationService;

public final class WebhookBackendDataMessageGeneratorJobWorker extends JobWorker {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(WebhookBackendDataMessageGeneratorJobWorker.class);

	private WebhookBackendDataMessageGeneratorService messageGenerator;
	
	@Override
	public final String runJob() throws Exception {
		this.messageGenerator = applicationContext.getBean("WebhookBackendDataMessageGeneratorService", WebhookBackendDataMessageGeneratorService.class);
		
		// TODO Just for testing the base class modifications
		checkForPrematureEnd();
		
		for(final WebhookEventType eventType : WebhookEventType.values()) {
			processEventType(eventType);
		}

		return null;
	}

	private final void processEventType(final WebhookEventType eventType) {
		final List<WebhookRegistryEntry> activeWebhooks = listActiveWebhooks(eventType);
		
		for(final WebhookRegistryEntry activeWebhook : activeWebhooks) {
			assert activeWebhook.getEventType() == eventType;
			
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Active webhook '%s' for company ID %d found", activeWebhook.getEventType(), activeWebhook.getCompanyId()));
			}
			
			try {
				this.messageGenerator.generateWebhookMessages(activeWebhook.getCompanyId(), activeWebhook.getEventType());
			} catch(final Exception e) {
				LOGGER.error(String.format("Error generating webhook messages from backend data (company ID: %d, event type: %s)", activeWebhook.getCompanyId(), activeWebhook.getEventType()));
			}
		}
	}

	private final List<WebhookRegistryEntry> listActiveWebhooks(final WebhookEventType eventType) {
		final WebhookRegistrationService registrationService = applicationContext.getBean("WebhookRegistrationService", WebhookRegistrationService.class);
		
		return registrationService.listAllRegisteredWebhookUrls(eventType);
	}

}
