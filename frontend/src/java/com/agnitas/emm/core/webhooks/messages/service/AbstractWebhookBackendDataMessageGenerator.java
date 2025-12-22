/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.agnitas.emm.core.commons.util.ConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.config.WebhookConfigService;
import com.agnitas.emm.core.webhooks.messages.common.WebhookBackendData;
import com.agnitas.emm.core.webhooks.messages.dao.WebhookBackendDataProcessTimestampDao;

public abstract class AbstractWebhookBackendDataMessageGenerator implements WebhookBackendDataMessageGenerator {

	private static final Logger LOGGER = LogManager.getLogger(AbstractWebhookBackendDataMessageGenerator.class);
	
	private WebhookBackendDataService backendDataService;
	protected WebhookMessageEnqueueService messageEnqueueService;
	protected WebhookBackendDataProcessTimestampDao processTimestampDao;
	protected WebhookConfigService configService;
	
	@Override
	public void generateWebhookMessages(final int companyID) {
		final Optional<ZonedDateTime> lastRunTimestampOpt = this.processTimestampDao.findTimestampOfLastRun(companyID, handledEventType());
		
		/*
		 * We have to subtract an amount of seconds to ensure, that all backend data will be converted.
		 * In some cases, it is likely (and happend already!), that backend data was inserted slightly before the next message generation run,
		 * but the commit of that data happend slightly after the data was selected by the message generator.
		 * In this case, the backend data was not selected in next message generation run.
		 * To avoid this, the grace period introduced.
		 */
		final ZonedDateTime now = ZonedDateTime.now().minusSeconds(configService.getMessageGenerationGracePeriodSeconds(companyID));

		if(lastRunTimestampOpt.isPresent()) {
			final List<WebhookBackendData> dataList = listBackendDataToProcess(companyID, handledEventType(), lastRunTimestampOpt.get(), now);
	
			for(final WebhookBackendData data : dataList) {
				try {
					generateWebhookMessage(data);
					deleteBackendData(data.getId());
				} catch(final Exception e) {
					LOGGER.error(String.format("Error converting backend data to webhook message (backend data ID: %d, company ID %d, event type: %s)", data.getId(), data.getCompanyID(), data.getEventType()));
				}
			}
		}
		
		this.processTimestampDao.updateTimestampOfLastRun(companyID, handledEventType(), now);
	}

	private final List<WebhookBackendData> listBackendDataToProcess(final int companyID, final WebhookEventType eventType, final ZonedDateTime fromInclusive, final ZonedDateTime toExclusive) {
		return this.backendDataService.listUnprocessedData(companyID, eventType, fromInclusive, toExclusive);
	}

	abstract void generateWebhookMessage(final WebhookBackendData data);

	private final void deleteBackendData(final long backendDataId) {
		this.backendDataService.deleteData(backendDataId);
	}

	public final void setWebhookBackendDataService(final WebhookBackendDataService service) {
		this.backendDataService = Objects.requireNonNull(service, "WebhookBackendDataService is null");
	}

	public final void setWebhookMessageEnqueueService(final WebhookMessageEnqueueService service) {
		this.messageEnqueueService = Objects.requireNonNull(service, "WebhookMessageEnqueueService is null");
	}

	public final void setWebhookProcessTimestampDao(final WebhookBackendDataProcessTimestampDao dao) {
		this.processTimestampDao = Objects.requireNonNull(dao, "WebhookProcessTimestampDao is null");
	}

	public final void setConfigService(final ConfigService service) {
		this.configService = new WebhookConfigService(service);
	}
}
