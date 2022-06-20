/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.landingpage.beans;

import java.util.Objects;

public final class RedirectSettings {

	private final String redirectUrl;
	private final int httpCode;
	
	public RedirectSettings(final String redirectUrl, final int httpCode) {
		this.redirectUrl = Objects.requireNonNull(redirectUrl, "URL is null");
		this.httpCode = httpCode;
	}
	
	public final int getHttpCode() {
		return this.httpCode;
	}
	
	public final String getRedirectUrl() {
		return this.redirectUrl;
	}
	
}
