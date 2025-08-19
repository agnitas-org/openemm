/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.profilefields.service;

import java.util.Set;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.messages.common.WebhookRecipientData;

public interface WebhookProfileFieldContentService {
	
	/**
	 * Returns the content of the profile fields for given customer.
	 * The list of profile fields must be configured.
	 * 
	 * <ul>
	 *   <li>Unknown profile fields are not included.</li>
	 *   <li>If the recipients does not exist, <code>null</code> is returned.</li>
	 *   <li>If no profile field is configured, an empty map is returned.</li>
	 *   <li>For recipients with tracking veto, an object is returning with 
	 *     <ul>
	 *       <li> {@link WebhookRecipientData#getRecipientId()} returning 0</li>
	 *       <li> {@link WebhookRecipientData#isTrackingVeto()} returning <code>true</code></li>
	 *       <li> {@link WebhookRecipientData#getProfileFields()} returning an empty map</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 * 
	 * If recipient ID 0 is given, it is assumed, that the recipient has tracking veto set.
	 * 
	 * @param companyID	company ID
	 * @param recipientID recipient ID
	 * @param eventType event type
	 * 
	 * @return profile field data
	 */
	WebhookRecipientData readProfileFields(final int companyID, final int recipientID, final WebhookEventType eventType);

	/**
	 * Extracts profile field names selected in the given webhook.
	 *
	 * @param eventType    type of webhook to inspect
	 * @param companyId    company ID associated with the webhook
	 * @return set of selected profile field names
	 */
	Set<String> getProfileFields(WebhookEventType eventType, int companyId);
}
