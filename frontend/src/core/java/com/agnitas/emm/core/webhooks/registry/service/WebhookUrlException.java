/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.registry.service;

public class WebhookUrlException extends RuntimeException {

	private static final long serialVersionUID = 1238776732731724929L;
	
	private final String url;
	
	public WebhookUrlException(String url, String msg) {
		super(msg);
		this.url = url;
	}
	
	public WebhookUrlException(String url, String msg, Throwable cause) {
		super(msg, cause);
		this.url = url;
	}

	public String getWebhookUrl() {
		return this.url;
	}

}
