/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.binding.service.event;

import java.util.Objects;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.webhooks.messages.service.WebhookMessageEnqueueService;
import com.agnitas.emm.common.UserStatus;

public final class BindingChangedWebhookHandler implements OnBindingChangedHandler {

	private WebhookMessageEnqueueService messageEnqueueService;
	
	/* 
	 * FIXME Replace setter-injection.
	 * 
	 * At the moment, there is a circular dependency, which prevents me from using constructor injection.
	 * The dependency has been introduced by this handler implementation.
	 * 
	 * By now (3rd Jan. 2023), the dependency graph is
	 * 
	 * - BindingEntryDao -> BindingChangeWebhookHandler
	 * - BindingChangeWebhookHandler -> WebhookMessageEnqueueService
	 * - WebhookMessageEnqueueService -> WebhookProfileFieldCOntentService
	 * - WebhookProfileFieldCOntentService -> recipientService
	 * - recipientService -> RecipientFactory
	 * - RecipientFactory -> BindingEntryFactory
	 * - BindingEntryFactory -> BindingEntryDao 
	 */
	public final void setWebhookMessageEnqueueService(final WebhookMessageEnqueueService service) {
		this.messageEnqueueService = Objects.requireNonNull(service);
	}
	
	@Override
	public void bindingCreated(int companyID, int recipientID, int mailinglistID, MediaTypes mediatype, UserStatus userStatus) {
		this.messageEnqueueService.enqueueRecipientBindingChanged(companyID, recipientID, mailinglistID, mediatype, userStatus);
	}

	@Override
	public void bindingChanged(int companyID, int recipientID, int mailinglistID, MediaTypes mediatype, UserStatus userStatus) {
		this.messageEnqueueService.enqueueRecipientBindingChanged(companyID, recipientID, mailinglistID, mediatype, userStatus);
	}
}
