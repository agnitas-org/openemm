/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.loginmanager.entity;

import java.util.Objects;
import java.util.Optional;

import org.agnitas.emm.core.logintracking.LoginStatus;
import org.agnitas.emm.core.logintracking.bean.LoginData;

public final class BlockedAddressData {
	
	private final int trackingId;
	
	private final String ipAddress;
	
	private final Optional<String> username;
	
	public BlockedAddressData(final int trackingId, final String ipAddress, final String usernameOrNull) {
		this.trackingId = trackingId;
		this.ipAddress = Objects.requireNonNull(ipAddress, "IP address is null");
		this.username = Optional.ofNullable(usernameOrNull);
	}

	public static Optional<BlockedAddressData> fromLoginData(final LoginData loginData) {
		if(loginData != null && loginData.getLoginStatus() == LoginStatus.SUCCESS_BUT_BLOCKED) {
			final String username = loginData.getUsername().orElse(null);
			
			return Optional.of(new BlockedAddressData(loginData.getLoginTrackId(), loginData.getLoginIP(), username));
		} else {
			return Optional.empty();
		}
	}

	public int getTrackingId() {
		return this.trackingId;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public Optional<String> getUsername() {
		return username;
	}
	
	public String getUsernameOrNull() {
		return username.orElse(null);
	}

	@Override
	public final String toString() {
		return username.isPresent()
				? String.format("blocked-ip(id=%d, ip=%s, user=%s)", trackingId, ipAddress, username.get())
				: String.format("blocked-ip(id=%d, ip=%s)", trackingId, ipAddress);
	}
}
