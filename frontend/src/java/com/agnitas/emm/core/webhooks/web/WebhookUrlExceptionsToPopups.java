/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.web;

import com.agnitas.emm.core.webhooks.registry.service.MalformedWebhookUrlException;
import com.agnitas.emm.core.webhooks.registry.service.NoHttpsWebhookUrlException;
import com.agnitas.emm.core.webhooks.registry.service.WebhookUrlException;
import com.agnitas.web.mvc.Popups;

/**
 * Utility class to create a message popup from a {@link WebhookUrlException}.
 */
final class WebhookUrlExceptionsToPopups {

	/**
	 * Create proper message popup for given {@link WebhookUrlException}.
	 * Does nothing, if {@code exception} or {@code popups} is null.
	 * 
	 * @param exception exception
	 * @param popups {@link Popups} to add popup message
	 */
	public static final void exceptionToPopup(final WebhookUrlException exception, final Popups popups) {
		if(exception != null && popups != null) {
			if(exception instanceof MalformedWebhookUrlException) {
				popups.alert("error.webhooks.url.malformed", exception.getWebhookUrl());
			} else if(exception instanceof NoHttpsWebhookUrlException) {
				popups.alert("error.webhooks.url.noHttps", exception.getWebhookUrl());
			} else {
				popups.alert("error.webhooks.url.general", exception.getWebhookUrl());
			}
		}
	}
	
}
