/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.event;

import java.util.Objects;

import com.agnitas.emm.core.mailtracking.service.event.OnLinkClickedHandler;
import com.agnitas.emm.core.webhooks.messages.service.WebhookMessageEnqueueService;

/**
 * Implementation of {@link OnLinkClickedHandler} interface
 * handling events for webhook interface.
 */
public final class LinkClickedWebhookHandler implements OnLinkClickedHandler {

	/** Service for enqueueing webhook messages. */
	private WebhookMessageEnqueueService messageEnqueueService;

	@Override
	public void handleLinkClicked(int companyID, int mailingID, int customerID, int linkID) {
		this.messageEnqueueService.enqueueLinkClickedMessage(companyID, mailingID, customerID, linkID);
	}

	/** 
	 * Set service for enqueueing webhook messages. 
	 * 	 
	 * @param service service for enqueueing webhook messages
	 */
	public void setWebhookMessageEnqueueService(WebhookMessageEnqueueService service) {
		this.messageEnqueueService = Objects.requireNonNull(service, "WebhookMessageEnqueueService is null");
	}

}
