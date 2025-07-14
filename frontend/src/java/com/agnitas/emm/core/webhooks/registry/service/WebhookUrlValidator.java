/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.registry.service;

import java.net.MalformedURLException;
import java.net.URL;

public final class WebhookUrlValidator {

	public void validateWebhookUrl(final String url) throws WebhookUrlException {
		final URL urlObj = toUrl(url);
		
		checkHttpsProtocol(urlObj);
	}
	
	private static final void checkHttpsProtocol(final URL url) throws WebhookUrlException {
		if(!"https".equalsIgnoreCase(url.getProtocol())) {
			throw WebhookUrlException.noHttpsProtocol(url.toString());
		}
	}
	
	private final URL toUrl(final String s) throws WebhookUrlException {
		try {
			return new URL(s);
		} catch (final MalformedURLException e) {
			throw WebhookUrlException.malformedUrl(s, e);
		}
	}
	
}
