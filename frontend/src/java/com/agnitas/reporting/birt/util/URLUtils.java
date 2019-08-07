/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class URLUtils {
	
	/**
	 * Simple wrapper on java.net.URLEncoder. It encodes URLs with UTF-8
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeURL(String url) throws UnsupportedEncodingException{
		return URLEncoder.encode(url, "UTF-8");
	}
	
	/**
	 * Simple wrapper on java.net.URL. It provides some convenience e.g. removes version info from protocol
	 * @param protocol
	 * @param hostname
	 * @param port
	 * @param contextPath
	 * @return
	 * @throws MalformedURLException
	 */
	public static String buildURL(String protocol, String hostname, int port, String contextPath ) throws MalformedURLException {
			
		URL url = new URL( getProtocolOnly(protocol), hostname, port,contextPath);		
		return url.toString();
	}
	
	
	/**
	 * removes the version info from the protocol: 'http/1.1' will become 'http' 
	 */
	private static String getProtocolOnly(String protocol) {
		if( protocol.contains("/")) {
			return protocol.substring(0, protocol.indexOf("/"));
		}
		return protocol;
	}
	
	
}
