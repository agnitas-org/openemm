/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.sessionhijacking.beans;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class ParsedIpSettings {

	private final InetAddress ip;
	private final Integer groupOrNull;
	
	public ParsedIpSettings(final IpSettings settings) throws UnknownHostException {
		this.ip = InetAddress.getByName(settings.getIp());
		this.groupOrNull = settings.getGroupOrNull();
	}

	public final InetAddress getIp() {
		return ip;
	}

	public final Integer getGroupOrNull() {
		return groupOrNull;
	}
	
}
