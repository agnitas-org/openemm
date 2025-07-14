/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistryEntry;

public final class WebhookRegistryEntryUtil {
	
	public static final List<WebhookListItem> toWebhookListItems(final List<WebhookRegistryEntry> configured) {
		return toWebhookListItems(configured, true);
	}
	
	public static final List<WebhookListItem> toWebhookListItems(final List<WebhookRegistryEntry> configured, final boolean includeUnconfigured) {
		final Map<WebhookEventType, WebhookRegistryEntry> byEventType = configured.stream()
				.collect(Collectors.toMap(hook -> hook.getEventType(), hook -> hook));

		final List<WebhookListItem> allWebhooks = new ArrayList<>();
		
		for(final WebhookEventType eventType : WebhookEventType.values()) {
			final WebhookRegistryEntry webhook = byEventType.get(eventType);
			
			if(webhook != null) {
				allWebhooks.add(WebhookListItem.from(webhook));
			} else {
				if(includeUnconfigured) {
					allWebhooks.add(WebhookListItem.from(eventType));
				}
			}
		}
		
		return allWebhooks;
	}
	
}
