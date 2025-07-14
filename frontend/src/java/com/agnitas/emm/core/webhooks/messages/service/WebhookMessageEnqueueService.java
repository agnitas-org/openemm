/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.service;

import java.time.ZonedDateTime;

import com.agnitas.emm.common.UserStatus;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;

/**
 * Service interface to enqueue different types of messages.
 */
public interface WebhookMessageEnqueueService {

	/**
	 * Enqueues a message for {@link WebhookEventType#MAILING_OPENED} event.
	 * 
	 * @param companyID company ID of opened mailing
	 * @param mailingID mailing ID of opened mailing
	 * @param customerID recipient ID of opened mailing
	 */
	void enqueueMailingOpenedMessage(int companyID, int mailingID, int customerID);

	/**
	 * Enqueues a message for {@link WebhookEventType#LINK_CLICKED} event.
	 * 
	 * @param companyID company ID of clicked link
	 * @param mailingID mailing ID of clicked link
	 * @param customerID recipient ID of clicked link
	 * @param linkID link ID of clicked link
	 */
	void enqueueLinkClickedMessage(int companyID, int mailingID, int customerID, int linkID);
	
	/**
	 * Enqueues a message for {@link WebhookEventType#MAILING_DELIVERED} event.
	 * 
	 * @param companyID company ID
	 * @param mailingID mailing ID
	 * @param customerID recipient ID
	 */
	void enqueueMailingDeliveredMessage(int companyID, int mailingID, int customerID, ZonedDateTime deliveryTimestamp);

	/**
	 * Enqueues a message for {@link WebhookEventType#TEST_MAILING_DELIVERED} event.
	 *
	 * @param companyID company ID
	 * @param mailingID mailing ID
	 * @param customerID recipient ID
	 */
	void enqueueTestMailingDeliveredMessage(int companyID, int mailingID, int customerID, ZonedDateTime deliveryTimestamp);


	/**
	 * Enqueues a message for {@link WebhookEventType#HARD_BOUNCE} event.
	 * 
	 * @param companyID company ID
	 * @param mailingID mailing ID
	 * @param customerID recipient ID
	 */
	void enqueueHardbounceMessage(int companyID, int mailingID, int customerID, ZonedDateTime timestamp);

	/**
	 * Enqueues a message for {@link WebhookEventType#MAILING_DELIVERY_COMPLETE} event.
	 * 
	 * @param companyID company ID
	 * @param mailingID mailing ID
	 */
	void enqueueMailingDeliveryCompleteMessage(int companyID, int mailingID, ZonedDateTime eventTimestamp);

	/**
	 * Enqueues a message for {@link WebhookEventType#BINDING_STATUS_CHANGED} event.
	 * 
	 * @param companyID company ID
	 * @param recipientID recipient ID
	 * @param mailinglistID mailinglist ID
	 * @param mediatypeOrNull media type or <code>null</code>
	 * @param userStatus user status
	 */
	void enqueueRecipientBindingChanged(int companyID, int recipientID, int mailinglistID, MediaTypes mediatypeOrNull, UserStatus userStatus);
}
