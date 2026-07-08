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

	public void validateWebhookUrl(String url) {
        checkHttpsProtocol(toUrl(url));
	}
	
	private static void checkHttpsProtocol(URL url) {
		if (!"https".equalsIgnoreCase(url.getProtocol())) {
			throw new NoHttpsWebhookUrlException(url.toString());
		}
	}
	
	private URL toUrl(String s) {
		try {
			return new URL(s);
		} catch (MalformedURLException e) {
			throw new MalformedWebhookUrlException(s, e);
		}
	}
	
}
