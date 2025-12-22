/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;

public final class WebhookBackendDataMessageGeneratorServiceImpl implements WebhookBackendDataMessageGeneratorService {

	private static final Logger LOGGER = LogManager.getLogger(WebhookBackendDataMessageGeneratorServiceImpl.class);
	
	private Map<WebhookEventType, WebhookBackendDataMessageGenerator> generatorMap;

	public WebhookBackendDataMessageGeneratorServiceImpl() {
		this.generatorMap = new HashMap<>();
	}
	
	@Override
	public void generateWebhookMessages(final int companyId, final WebhookEventType eventType) {
		final WebhookBackendDataMessageGenerator generator = this.generatorMap.get(eventType);
		
		if(generator != null) {
			generator.generateWebhookMessages(companyId);
		} else {
			LOGGER.info("No webhook message generator for event type {}. Backend data remains in table.", eventType);
		}
	}
	
	public final void setWebhookBackendDataMessageGenerators(final Collection<WebhookBackendDataMessageGenerator> generators) {
		final Map<WebhookEventType, WebhookBackendDataMessageGenerator> newMap = new HashMap<>();
		
		for(final WebhookBackendDataMessageGenerator generator : generators) {
			if(newMap.containsKey(generator.handledEventType())) {
				throw new IllegalArgumentException(String.format("Multiple WebhookBackendDataMessageGenerators handling event '%s'", generator.handledEventType()));
			}
			
			newMap.put(generator.handledEventType(), generator);
		}
		
		this.generatorMap = newMap;
	}

}
