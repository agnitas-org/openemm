/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.common;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class WebhookRecipientData {

	private final int recipientId;
	private final boolean trackingVeto;
	private final Map<String, String> profileFields;
	
	private WebhookRecipientData(final int recipientId, final boolean trackingVeto, final Map<String, String> profileFields) {
		this.recipientId = recipientId;
		this.trackingVeto = trackingVeto;
		this.profileFields = Collections.unmodifiableMap(Objects.requireNonNull(profileFields, "profileFields is null"));
	}

	public static final WebhookRecipientData trackableRecipient(final int recipientId, final Map<String, String> profileFields) {
		return new WebhookRecipientData(recipientId, false, profileFields);
	}
	
	public static final WebhookRecipientData trackingVeto() {
		return new WebhookRecipientData(0, true, Collections.emptyMap());
	}
	
	public final int getRecipientId() {
		return recipientId;
	}
	
	public final boolean isTrackingVeto() {
		return trackingVeto;
	}
	
	public final Map<String, String> getProfileFields() {
		return profileFields;
	}
	
}
