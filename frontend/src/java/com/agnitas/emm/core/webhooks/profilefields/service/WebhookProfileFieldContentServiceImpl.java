/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.profilefields.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Recipient;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.messages.common.WebhookRecipientData;
import com.agnitas.emm.core.webhooks.profilefields.dao.WebhookProfileFieldDao;

public final class WebhookProfileFieldContentServiceImpl implements WebhookProfileFieldContentService {

	private WebhookProfileFieldDao profileFieldDao;
	private RecipientService recipientService;

	@Override
	public Set<String> getProfileFields(WebhookEventType eventType, int companyId) {
		return this.profileFieldDao.listProfileFieldsForWebhook(companyId, eventType);
	}

	@Override
	public final WebhookRecipientData readProfileFields(final int companyID, final int customerID, final WebhookEventType eventType) {
		// Customer ID is assumed to be a recipient with tracking veto set
		if(customerID == 0) {
			return WebhookRecipientData.trackingVeto();
		}
		
		// Read the recipient data
		final Recipient recipient = this.recipientService.getRecipient(companyID, customerID);
		
		// Tracking veto flag set?
		if(recipient.isDoNotTrackMe()) {
			return WebhookRecipientData.trackingVeto();
		}
		
		// Read profile fields to publish
		final Set<String> profileFieldsToPublish = this.profileFieldDao.listProfileFieldsForWebhook(companyID, eventType);
		
		// No profile fields to publish? Return empty map.
		if(profileFieldsToPublish.isEmpty()) {
			return WebhookRecipientData.trackableRecipient(recipient.getCustomerID(), Collections.emptyMap());
		}
		
		// Convert field names to lower case
		final Set<String> profileFields = profileFieldsToPublish
				.stream()
				.map(s -> s.toLowerCase())
				.distinct()
				.collect(Collectors.toSet());
			
		final Map<String, Object> customerData = recipient.getCustParameters();
		
		// Publish data from listed profile fields only
		final Map<String, String> data = new HashMap<>();
		for(final Map.Entry<String, Object> entry : customerData.entrySet()) {
			if(profileFields.contains(entry.getKey().toLowerCase())) {
				data.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
			}
		}
		
		return WebhookRecipientData.trackableRecipient(recipient.getCustomerID(), data);
	}

	public final void setWebhookProfileFieldDao(final WebhookProfileFieldDao dao) {
		this.profileFieldDao = Objects.requireNonNull(dao, "WebhookProfileFieldDao is null");
	}
	
	public final void setRecipientService(final RecipientService service) {
		this.recipientService = Objects.requireNonNull(service, "RecipientService is null");
	}
}
