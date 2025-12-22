/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.sender;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.config.WebhookConfigService;
import com.agnitas.emm.core.webhooks.messages.common.WebhookMessage;
import com.agnitas.emm.core.webhooks.messages.service.WebhookMessageDequeueService;
import com.agnitas.emm.core.webhooks.registry.common.WebhookNotRegisteredException;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistryEntry;
import com.agnitas.emm.core.webhooks.registry.service.WebhookRegistrationService;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Implementation of {@link WebhookMessageSender} interface.
 */
public final class WebhookMessageSenderImpl implements WebhookMessageSender {

	private static final Logger LOGGER = LogManager.getLogger(WebhookMessageSenderImpl.class);

	/** Date format for timestamps. Format is '2011-12-03T10:15:30Z' */
	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT;

	/** Name of JSON element containing number of events. */
	public static final String EVENT_COUNT_JSON_PROPERTY = "event_count";

	/** Name of JSON element containing type of events. */
	public static final String EVENT_TYPE_JSON_PROPERTY = "event_type";

	/** Name of JSON element containing events. */
	public static final String EVENTS_JSON_PROPERTY = "events";

	/** Name of JSON element containing event ID. */
	public static final String EVENT_ID_JSON_PROPERTY = "event_id";

	/** Name of JSON element containing event timestamp. */
	public static final String EVENT_TIMESTAMP_JSON_PROPERTY = "event_timestamp";

	/** Name of JSON element containing event data. */
	public static final String EVENT_DATA_JSON_PROPERTY = "event_data";

	/** Registration service for webhook URLs. */
	private WebhookRegistrationService registrationService;

	/** Service for retrieving messages from queue. */
	private WebhookMessageDequeueService dequeueService;

	/** Factory creating fully configured {@link CloseableHttpClient}s. */
	private WebhookHttpClientFactory httpClientFactory;

	/** Configuration service wrapper for webhooks. */
	private WebhookConfigService configService;

	@Override
	public boolean sendNextMessagePackage(Date limitDate) {
		final List<WebhookMessage> messages = dequeueService.listAndMarkMessagesForSending(limitDate);

		if (messages == null || messages.isEmpty()) {
			// No (more) message found to send
			return false;
		} else {
			final WebhookEventType eventType = messages.get(0).getEventType();
			final int companyID = messages.get(0).getCompanyId();

			try {
				try {
					if (!configService.isWebhookInterfaceEnabled(companyID)) {
						dequeueService.markMessagesAsSendFailed(messages, "Webhook interface disabled");
					} else {
						final WebhookRegistryEntry webhookRegistryEntry = registrationService.findWebhookEntry(companyID, eventType);

						assert webhookRegistryEntry.getCompanyId() == messages.get(0).getCompanyId();
						assert webhookRegistryEntry.getEventType() == messages.get(0).getEventType();
						assert webhookRegistryEntry.getUrl() != null;

						sendMessageBlock(messages, webhookRegistryEntry);
					}
				} catch (WebhookNotRegisteredException e) {
					final String note = String.format(
							"No webhook registered (event type: '%s', company ID %d)",
							eventType.name(),
							companyID);

					dequeueService.cancelMessages(messages, note);
				}
			} catch (Exception e) {
				LOGGER.error("Error processing webhook messages", e);
			}

			return true;
		}
	}

	/**
	 * Sends given messages as a block. All messages must be of same company ID and same event type.
	 *
	 * @param messages messages to sent
	 * @param webhookRegistryEntry webhook configuration
	 */
	private void sendMessageBlock(List<WebhookMessage> messages, WebhookRegistryEntry webhookRegistryEntry) {
		final JSONObject completeMessage = createCompleteMessageJsonObject(messages, webhookRegistryEntry);

		if (configService.useAsyncClient(webhookRegistryEntry.getCompanyId())) {
			sendJson(
					completeMessage.toString(),
					webhookRegistryEntry,
					() -> dequeueService.markMessagesAsSent(messages),
					note -> dequeueService.markMessagesAsSendFailed(messages, note)
            );

			return;
		}

		try {
			sendJson(completeMessage, webhookRegistryEntry);

			this.dequeueService.markMessagesAsSent(messages);
		} catch (Exception e) {
			final String note = String.format(
					"Error sending webhook message (event type: '%s', company ID %d, error: '%s')",
					webhookRegistryEntry.getEventType().name(),
					webhookRegistryEntry.getCompanyId(),
					e.getMessage());

			LOGGER.error(note, e);

			this.dequeueService.markMessagesAsSendFailed(messages, note);
		}
	}

	/**
	 * Send JSON document.
	 *
	 * @param completeMessage JSON document containing webhook message
	 * @param webhookRegistryEntry webhook configuration
	 *
	 * @throws Exception on errors sending JSON
	 */
	private void sendJson(JSONObject completeMessage, WebhookRegistryEntry webhookRegistryEntry) throws Exception {
		LOGGER.info("Sending webhook message to '{}'", webhookRegistryEntry.getUrl());

		try (CloseableHttpClient client = this.httpClientFactory.createHttpClient(webhookRegistryEntry.getCompanyId())) {
			final HttpEntity entity = EntityBuilder.create()
					.setText(completeMessage.toString())
					.setContentType(ContentType.APPLICATION_JSON)
					.build();

			final HttpPost request = new HttpPost(webhookRegistryEntry.getUrl());
			request.setEntity(entity);

			try (CloseableHttpResponse response = client.execute(request)) {
				int statusCode = response.getCode();
				String reasonPhrase = response.getReasonPhrase();

				LOGGER.debug("Client responded with status code {} (reason: '{}')", statusCode, reasonPhrase);

				if (statusCode != 200) {
					throw new Exception(String.format("Client responded with status code %d (reason: '%s')", statusCode, reasonPhrase));
				}
			}
		}
	}

	private void sendJson(String json, WebhookRegistryEntry webhookRegistryEntry, Runnable successCallback, Consumer<String> errorCallback) {
		LOGGER.info("Sending webhook message to '{}'", webhookRegistryEntry.getUrl());

        CloseableHttpAsyncClient client;
        try {
            client = httpClientFactory.createHttpAsyncClient(webhookRegistryEntry.getCompanyId());
			client.start();
        } catch (WebhookHttpClientFactoryException e) {
			String note = String.format("Error sending webhook message (event type: '%s', company ID %d, error: '%s')",
					webhookRegistryEntry.getEventType().name(), webhookRegistryEntry.getCompanyId(), e.getMessage());

			LOGGER.error(note, e);
			errorCallback.accept(note);
			return;
        }

		SimpleHttpRequest request = SimpleRequestBuilder.post(webhookRegistryEntry.getUrl())
				.setBody(json, ContentType.APPLICATION_JSON)
				.build();

		client.execute(request, new FutureCallback<>() {
            @Override
            public void completed(SimpleHttpResponse response) {
                int statusCode = response.getCode();
                String reason = response.getReasonPhrase();

				LOGGER.debug("Client responded with status code {} (reason: '{}')", statusCode, reason);

                if (statusCode == 200) {
					successCallback.run();
                } else {
                    errorCallback.accept(String.format("Client responded with status code %d (reason: '%s')", statusCode, reason));
					LOGGER.error("Client responded with status code {} (reason: '{}')", statusCode, reason);
				}

				tryCloseHttpClient(client);
            }

            @Override
            public void failed(Exception e) {
				String note = String.format("Error sending webhook message (event type: '%s', company ID %d, error: '%s')",
						webhookRegistryEntry.getEventType().name(), webhookRegistryEntry.getCompanyId(), e.getMessage());

				LOGGER.error(note, e);
				errorCallback.accept(note);

				tryCloseHttpClient(client);
            }

            @Override
            public void cancelled() {
                String note = String.format("Error sending webhook message due to cancelled request (event type: '%s', company ID %d)",
                        webhookRegistryEntry.getEventType().name(), webhookRegistryEntry.getCompanyId());

                errorCallback.accept(note);
                LOGGER.error(note);

                tryCloseHttpClient(client);
            }
        });
	}

	private void tryCloseHttpClient(CloseableHttpAsyncClient client) {
		try {
			client.close();
		} catch (Exception e) {
			LOGGER.warn("Failed to close HTTP client", e);
		}
	}

	/**
	 * Creates the JSON document for the complete webhook message.
	 *
	 * @param messages event messages
	 * @param webhookRegistryEntry webhook configuration
	 *
	 * @return JSON document
	 */
	private JSONObject createCompleteMessageJsonObject(List<WebhookMessage> messages, WebhookRegistryEntry webhookRegistryEntry) {
		final JSONObject completeMessage = new JSONObject();

		completeMessage.put(EVENT_COUNT_JSON_PROPERTY, messages.size());
		completeMessage.put(EVENT_TYPE_JSON_PROPERTY, webhookRegistryEntry.getEventType().getStringRepresentation());
		completeMessage.put(EVENTS_JSON_PROPERTY, createMessagesJsonArray(messages, webhookRegistryEntry));

		return completeMessage;
	}

	/**
	 * Converts given event messages to a JSON array.
	 *
	 * @param messages event messages
	 * @param webhookRegistryEntry webhook configuration
	 *
	 * @return JSON array
	 */
	private JSONArray createMessagesJsonArray(List<WebhookMessage> messages, WebhookRegistryEntry webhookRegistryEntry) {
		final JSONArray array = new JSONArray();

		for (WebhookMessage message : messages) {
			array.put(createMessageJson(message, webhookRegistryEntry));
		}

		return array;
	}

	/**
	 * Checks if message meets webhook configuration.
	 * The message must have equal company ID and event type.
	 *
	 * @param message event message
	 * @param webhookRegistryEntry webhook configuration
	 *
	 * @throws RuntimeException if company ID or event type differs
	 */
	private static void checkMessage(WebhookMessage message, WebhookRegistryEntry webhookRegistryEntry) {
		if(message.getEventType() != webhookRegistryEntry.getEventType()) {
			final String msg = String.format(
					"Encountered webhook message of invalid event type (found: '%s', expected '%s', message ID: %d)",
					message.getEventType(),
					webhookRegistryEntry.getEventType(),
					message.getEventId());

			LOGGER.error(msg);

			throw new RuntimeException(msg);
		}

		if(message.getCompanyId() != webhookRegistryEntry.getCompanyId()) {
			final String msg = String.format(
					"Encountered webhook message of invalid copmany ID (found: %d, expected %d, message ID: %d)",
					message.getCompanyId(),
					webhookRegistryEntry.getCompanyId(),
					message.getEventId());

			LOGGER.error(msg);

			throw new RuntimeException(msg);
		}
	}

	/**
	 * Converts given event message to JSON object.
	 *
	 * @param message event message
	 * @param webhookRegistryEntry webhook configuration
	 *
	 * @return JSON object
	 */
	private JSONObject createMessageJson(WebhookMessage message, WebhookRegistryEntry webhookRegistryEntry) {
		checkMessage(message, webhookRegistryEntry);

		JSONObject payload = new JSONObject(message.getPayload());
		final ZonedDateTime utcTimestamp = message.getEventTimestamp().withZoneSameInstant(ZoneOffset.UTC);

		final JSONObject obj = new JSONObject();

		obj.put(EVENT_ID_JSON_PROPERTY, message.getEventId());
		obj.put(EVENT_TIMESTAMP_JSON_PROPERTY, utcTimestamp.format(TIMESTAMP_FORMATTER));
		obj.put(EVENT_DATA_JSON_PROPERTY, payload);

		return obj;
	}

	/**
	 * Set registration service for webhook URLs.
	 *
	 * @param service registration service for webhook URLs
	 */
	public void setWebhookRegistrationService(WebhookRegistrationService service) {
		this.registrationService = Objects.requireNonNull(service, "WebhookRegistrationService is null");
	}

	/**
	 * Set service for retrieving messages from queue.
	 *
	 * @param service service for retrieving messages from queue
	 */
	public void setWebhookMessageDequeueService(WebhookMessageDequeueService service) {
		this.dequeueService = Objects.requireNonNull(service, "WebhookMessageDequeueService is null");
	}

	/**
	 * Set factory that creates fully configured {@link CloseableHttpClient}s.
	 *
	 * @param factory factory that creates fully configured {@link CloseableHttpClient}s
	 */
	public void setWebhookHttpClientFactory(WebhookHttpClientFactory factory) {
		this.httpClientFactory = Objects.requireNonNull(factory, "WebhookHttpClientFactory is null");
	}

	/**
	 * Set configuration service.
	 *
	 * @param service configuration service.
	 */
	public void setConfigService(ConfigService service) {
		this.configService = new WebhookConfigService(service);
	}
}
