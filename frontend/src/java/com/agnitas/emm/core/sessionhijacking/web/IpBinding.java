/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.sessionhijacking.web;

import java.net.InetAddress;

import jakarta.servlet.http.HttpSession;

/**
 * Utility class to bind session and IP address.
 */
final class IpBinding {

	/** Name of the session attribute containing the bound IP address. */
	public static final String IP_ATTRIBUTE = GroupingSessionHijackingPreventionFilter.class.getCanonicalName() + ".IP_ADDRESS";

	/**
	 * Returns the IP address bound to given session.
	 * 
	 * @param session HTTP session
	 * 
	 * @return bound IP address or <code>null</code>
	 */
	static final InetAddress getBoundIpAddress(final HttpSession session) {
		return (InetAddress) session.getAttribute(IP_ATTRIBUTE);
	}
	
	/**
	 * Binds IP address to HTTP session.
	 * 
	 * @param session HTTP session
	 * @param address IP address
	 */
	static final void bindIpAddress(final HttpSession session, final InetAddress address) {
		session.setAttribute(IP_ATTRIBUTE, address);
	}

}
