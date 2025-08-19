/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.service;

import static com.agnitas.emm.core.webhooks.common.WebhookEventType.PROFILE_FIELD_CHANGED;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.service.RecipientProfileHistoryService;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.config.WebhookConfigService;
import com.agnitas.emm.core.webhooks.messages.common.JsonMessageUtils;
import com.agnitas.emm.core.webhooks.messages.common.WebhookRecipientData;
import com.agnitas.emm.core.webhooks.messages.dao.WebhookMessageQueueDao;
import com.agnitas.emm.core.webhooks.profilefields.service.WebhookProfileFieldContentService;
import com.agnitas.emm.core.webhooks.registry.common.WebhookNotRegisteredException;
import com.agnitas.emm.core.webhooks.registry.service.WebhookRegistrationService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Implementation of {@link WebhookMessageEnqueueService} interface.
 */
public final class WebhookMessageEnqueueServiceImpl implements WebhookMessageEnqueueService {

	private static final Logger LOGGER = LogManager.getLogger(WebhookMessageEnqueueServiceImpl.class);

	/** Configuration service. */
	private final WebhookConfigService configService;
	
	/** Service to handle webhook URL registrations. */
	private final WebhookRegistrationService registrationService;
	
	/** Service to access selected recipient profile fields. */
	private final WebhookProfileFieldContentService webhookProfileFieldService;
	
	/** DAO for storing and loading messages. */
	private final WebhookMessageQueueDao messageQueueDao;

	private final RecipientProfileHistoryService recipientProfileHistoryService;

	public WebhookMessageEnqueueServiceImpl(ConfigService configService, WebhookRegistrationService registrationService,
											WebhookProfileFieldContentService profileFieldService,
											WebhookMessageQueueDao messageQueueDao,
											RecipientProfileHistoryService recipientProfileHistoryService) {
		this.configService = new WebhookConfigService(configService);
		this.registrationService = Objects.requireNonNull(registrationService);
		this.webhookProfileFieldService = Objects.requireNonNull(profileFieldService);
		this.messageQueueDao = Objects.requireNonNull(messageQueueDao);
		this.recipientProfileHistoryService = recipientProfileHistoryService;
	}
	
	@Override
	public void enqueueMailOpenedMessage(int companyID, int mailingID, int customerID) {
		final WebhookRecipientData profileFields = this.webhookProfileFieldService.readProfileFields(companyID, customerID, WebhookEventType.MAIL_OPENED);
		
		final JSONObject jsonPayload = new JSONObject();
		
		JsonMessageUtils.addMailingId(jsonPayload, mailingID);
		JsonMessageUtils.addRecipientIdAndData(jsonPayload, profileFields);
		
		enqueueMessage(WebhookEventType.MAIL_OPENED, companyID, jsonPayload);
	}

	@Override
	public void enqueueLinkClickedMessage(int companyID, int mailingID, int customerID, int linkID) {
		final WebhookRecipientData profileFields = this.webhookProfileFieldService.readProfileFields(companyID, customerID, WebhookEventType.LINK_CLICKED);

		final JSONObject jsonPayload = new JSONObject();

		JsonMessageUtils.addMailingId(jsonPayload, mailingID);
		JsonMessageUtils.addRecipientIdAndData(jsonPayload, profileFields);
		JsonMessageUtils.addLinkId(jsonPayload, linkID);
		
		enqueueMessage(WebhookEventType.LINK_CLICKED, companyID, jsonPayload);
	}
	
	@Override
	public void enqueueMailDeliveredMessage(int companyID, int mailingID, int customerID, ZonedDateTime deliveryTimestamp) {
		final WebhookRecipientData profileFields = this.webhookProfileFieldService.readProfileFields(companyID, customerID, WebhookEventType.MAIL_DELIVERED);

		final JSONObject jsonPayload = new JSONObject();

		JsonMessageUtils.addMailingId(jsonPayload, mailingID);
		JsonMessageUtils.addRecipientIdAndData(jsonPayload, profileFields);
		
		enqueueMessage(WebhookEventType.MAIL_DELIVERED, companyID, jsonPayload, deliveryTimestamp);
	}

	@Override
	public void enqueueTestMailDeliveredMessage(int companyID, int mailingID, int customerID, ZonedDateTime deliveryTimestamp) {
		WebhookRecipientData profileFields = webhookProfileFieldService.readProfileFields(companyID, customerID, WebhookEventType.TEST_MAIL_DELIVERED);

		JSONObject jsonPayload = new JSONObject();

		JsonMessageUtils.addMailingId(jsonPayload, mailingID);
		JsonMessageUtils.addRecipientIdAndData(jsonPayload, profileFields);

		enqueueMessage(WebhookEventType.TEST_MAIL_DELIVERED, companyID, jsonPayload, deliveryTimestamp);
	}

	@Override
	public void enqueueHardbounceMessage(int companyID, int mailingID, int customerID, ZonedDateTime timestamp) {
		final WebhookRecipientData profileFields = this.webhookProfileFieldService.readProfileFields(companyID, customerID, WebhookEventType.HARD_BOUNCE);

		final JSONObject jsonPayload = new JSONObject();

		JsonMessageUtils.addMailingId(jsonPayload, mailingID);
		JsonMessageUtils.addRecipientIdAndData(jsonPayload, profileFields);
		
		enqueueMessage(WebhookEventType.HARD_BOUNCE, companyID, jsonPayload, timestamp);
	}

	@Override
	public void enqueueMailingDeliveryCompleteMessage(int companyID, int mailingID, ZonedDateTime timestamp) {
		final JSONObject jsonPayload = new JSONObject();

		JsonMessageUtils.addMailingId(jsonPayload, mailingID);
		
		enqueueMessage(WebhookEventType.MAILING_DELIVERY_COMPLETE, companyID, jsonPayload, timestamp);
	}

	@Override
	public void enqueueProfileFieldChangedMessages(int companyId, ZonedDateTime lastStart) {
		if (!recipientProfileHistoryService.isProfileFieldHistoryEnabled(companyId)) {
			return;
		}
		Set<String> fields = webhookProfileFieldService.getProfileFields(PROFILE_FIELD_CHANGED, companyId);
		recipientProfileHistoryService.getChangedRecipients(fields, lastStart, companyId).stream()
			.map(id -> webhookProfileFieldService.readProfileFields(companyId, id, PROFILE_FIELD_CHANGED))
			.forEach(recipientData -> {
				JSONObject jsonPayload = new JSONObject();
				JsonMessageUtils.addRecipientIdAndData(jsonPayload, recipientData);
				enqueueMessage(PROFILE_FIELD_CHANGED, companyId, jsonPayload, lastStart);
			});
	}

	@Override
	public void enqueueRecipientBindingChanged(int companyID, int recipientID, int mailinglistID, MediaTypes mediatypeOrNull, UserStatus userStatus) {
		final WebhookRecipientData profileFields = this.webhookProfileFieldService.readProfileFields(companyID, recipientID, WebhookEventType.BINDING_STATUS_CHANGED);

		final JSONObject jsonPayload = new JSONObject();

		JsonMessageUtils.addMailinglistId(jsonPayload, mailinglistID);
		JsonMessageUtils.addMediaType(jsonPayload, mediatypeOrNull);
		JsonMessageUtils.addUserStatus(jsonPayload, userStatus);		
		JsonMessageUtils.addRecipientIdAndData(jsonPayload, profileFields);
		
		enqueueMessage(WebhookEventType.BINDING_STATUS_CHANGED, companyID, jsonPayload);
	}

	/**
	 * Enqueues a message with current timestamp.
	 * 
	 * @param type event type
	 * @param companyID company ID
	 * @param payload payload for message (JSON formatted)
	 */
	private void enqueueMessage(WebhookEventType type, int companyID, JSONObject payload) {
		enqueueMessage(type, companyID, payload, ZonedDateTime.now());
	}
	
	/**
	 * Enqueues a message with given event timestamp.
	 * 
	 * @param type event type
	 * @param companyID company ID
	 * @param payload payload for message (JSON formatted)
	 * @param eventTimestamp event timestamp
	 */
	private void enqueueMessage(WebhookEventType type, int companyID, JSONObject payload, ZonedDateTime eventTimestamp) {
		// Webhook active?
		if(!isWebhookActive(companyID, type)) {
			return;		// Not active, stop here
		}
		
		this.messageQueueDao.enqueueMessage(companyID, type, eventTimestamp, payload);
	}
	
	/**
	 * Checks if all pre-requisites for sending webhook message are satisfied.
	 * 
	 * @param companyID company ID
	 * @param type event type
	 * 
	 * @return <code>true</code> if all pre-requisited are satisfied, otherwise <code>false</code>
	 */
	private final boolean isWebhookActive(final int companyID, final WebhookEventType type) {
		// Check if webhooks interface is enabled
		if(!configService.isWebhookInterfaceEnabled(companyID)) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Webhooks disabled for company ID %d", companyID));
			}
			
			return false;
		}
		
		// Check if webhook URL is configured for given event type
		if(!isWebhookUrlConfigured(companyID, type)) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("WebhookURL configured for event type '%s' for company ID %d", type, companyID));
			}
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks, if a webhook URL is configured for company ID and event type.
	 * 
	 * @param companyID company ID
	 * @param type event type
	 * 
	 * @return <code>true</code> if URL is configured, otherwise <code>false</code>
	 */
	private final boolean isWebhookUrlConfigured(final int companyID, final WebhookEventType type) {
		try {
			this.registrationService.findWebhookEntry(companyID, type);
			
			return true;
		} catch(final WebhookNotRegisteredException e) {
			return false;
		}
	}

}
