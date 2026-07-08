/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.common;

public enum HostAuthenticationCookieExpirationSettings {

	EXPIRE_30(30),
	EXPIRE_90(90),
	EXPIRE_180(180);
	
	public static final HostAuthenticationCookieExpirationSettings DEFAULT = EXPIRE_180;
	
	private final int expireDays;
	
	HostAuthenticationCookieExpirationSettings(final int days) {
		this.expireDays = days;
	}
	
	public final int getExpireDays() {
		return this.expireDays;
	}
	
	public static final HostAuthenticationCookieExpirationSettings findByExpireDays(final int days) {
		for(final HostAuthenticationCookieExpirationSettings value : values()) {
			if(value.expireDays == days) {
				return value;
			}
		}
		
		return DEFAULT;
	}
	
}
