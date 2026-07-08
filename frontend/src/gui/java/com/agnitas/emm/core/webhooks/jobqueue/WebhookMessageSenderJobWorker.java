/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.jobqueue;

import java.util.Date;

import com.agnitas.emm.core.webhooks.sender.WebhookMessageSender;
import com.agnitas.service.JobWorkerBase;
import com.agnitas.util.quartz.JobWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Job worker for sending webhook messages.
 */
@JobWorker("WebhookMessageDelivery")
public final class WebhookMessageSenderJobWorker extends JobWorkerBase {

	private static final Logger LOGGER = LogManager.getLogger(WebhookMessageSenderJobWorker.class);
	
	@Override
	public final String runJob() throws Exception {
		final WebhookMessageSender sender = applicationContext.getBean("WebhookMessageSender", WebhookMessageSender.class);
		
		Date limitDate = job.getLastStart();
		
		while (sender.sendNextMessagePackage(limitDate)) {
			LOGGER.info("Found webhook messages to send. Looking for next block.");
			// Repeat until no more messages found
			
			checkForPrematureEnd();
		}

		LOGGER.info("No more webhook messages to send. Going to sleep.");

		return null;
	}

}
