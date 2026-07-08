/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.settings.common;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;

public final class WebhookSettings {

	private final WebhookEventType eventType;
	private final String url;
	private final List<String> profileFields;
	
	public WebhookSettings(final WebhookEventType eventType, final String url, final List<String> profileFields) {
		this.eventType = Objects.requireNonNull(eventType, "Webhook event type is null");
		this.url = url != null ? url : "";
		this.profileFields = profileFields != null ? Collections.unmodifiableList(profileFields) : Collections.emptyList();
	}

	public final WebhookEventType getEventType() {
		return eventType;
	}

	public final String getUrl() {
		return url;
	}

	public final List<String> getProfileFields() {
		return profileFields;
	}

}
