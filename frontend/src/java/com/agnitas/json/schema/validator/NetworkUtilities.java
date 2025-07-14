/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class NetworkUtilities {
	private static final String DOMAIN_PART_REGEX = "\\p{Alnum}(?>[\\p{Alnum}-]*\\p{Alnum})*";
	private static final String TOP_DOMAIN_PART_REGEX = "\\p{Alpha}{2,}";
	private static final String DOMAIN_NAME_REGEX = "^(?:" + DOMAIN_PART_REGEX + "\\.)+" + "(" + TOP_DOMAIN_PART_REGEX + ")$";

	private static final Pattern IPV4_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	private static final Pattern IPV6_PATTERN = Pattern.compile(
			"("
					+ "([0-9A-F]{1,4}:){7,7}[0-9A-F]{1,4}|"          //# 1:2:3:4:5:6:7:8
					+ "([0-9A-F]{1,4}:){1,7}:|"                      //# 1::                              1:2:3:4:5:6:7::
					+ "([0-9A-F]{1,4}:){1,6}:[0-9A-F]{1,4}|"         //# 1::8             1:2:3:4:5:6::8  1:2:3:4:5:6::8
					+ "([0-9A-F]{1,4}:){1,5}(:[0-9A-F]{1,4}){1,2}|"  //# 1::7:8           1:2:3:4:5::7:8  1:2:3:4:5::8
					+ "([0-9A-F]{1,4}:){1,4}(:[0-9A-F]{1,4}){1,3}|"  //# 1::6:7:8         1:2:3:4::6:7:8  1:2:3:4::8
					+ "([0-9A-F]{1,4}:){1,3}(:[0-9A-F]{1,4}){1,4}|"  //# 1::5:6:7:8       1:2:3::5:6:7:8  1:2:3::8
					+ "([0-9A-F]{1,4}:){1,2}(:[0-9A-F]{1,4}){1,5}|"  //# 1::4:5:6:7:8     1:2::4:5:6:7:8  1:2::8
					+ "[0-9A-F]{1,4}:((:[0-9A-F]{1,4}){1,6})|"       //# 1::3:4:5:6:7:8   1::3:4:5:6:7:8  1::8
					+ ":((:[0-9A-F]{1,4}){1,7}|:)|"                  //# ::2:3:4:5:6:7:8  ::2:3:4:5:6:7:8 ::8       ::
					+ "FE80:(:[0-9A-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|"  //# fe80::7:8%eth0   fe80::7:8%1     (link-local IPv6 addresses with zone index)
					+ "::(FFFF(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|" //# ::255.255.255.255   ::ffff:255.255.255.255  ::ffff:0:255.255.255.255  (IPv4-mapped IPv6 addresses and IPv4-translated addresses)
					+ "([0-9A-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])" //# 2001:db8:3:4::192.0.2.33  64:ff9b::192.0.2.33 (IPv4-Embedded IPv6 Address)
					+ ")", Pattern.CASE_INSENSITIVE
			);

	private static final Pattern DOMAIN_NAME_PATTERN = Pattern.compile(DOMAIN_NAME_REGEX);

	public static byte[] getMacAddressBytes(final String macAddress) throws IllegalArgumentException {
		if (StringUtils.isEmpty(macAddress)) {
			throw new IllegalArgumentException("Invalid MAC address.");
		}

		final String[] hexParts = macAddress.split("(\\:|\\-| )");
		if (hexParts.length != 6) {
			throw new IllegalArgumentException("Invalid MAC address.");
		}

		try {
			final byte[] bytes = new byte[6];
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) Integer.parseInt(hexParts[i], 16);
			}
			return bytes;
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Invalid hex digit in MAC address.", e);
		}
	}

	public static String getHostnameFromRequestString(String requestString) {
		if (requestString == null || !requestString.contains("/")) {
			return requestString;
		} else {
			if (requestString.toLowerCase().startsWith("http")) {
				requestString = requestString.substring(requestString.indexOf("//") + 2);

				if (!requestString.contains("/")) {
					return requestString;
				}
			}

			return requestString.substring(0, requestString.indexOf("/"));
		}
	}

	public static boolean isValidDomain(final String domain) {
		String asciiDomainName;
		try {
			asciiDomainName = java.net.IDN.toASCII(domain);
		} catch (final Exception e) {
			// invalid domain name like abc@.ch
			return false;
		}

		// Do not allow ".local" top level domain
		if (asciiDomainName.toLowerCase().endsWith(".local")) {
			return false;
		}

		return DOMAIN_NAME_PATTERN.matcher(asciiDomainName).matches();
	}


	public static boolean isValidHostname(final String value) {
		return isValidDomain(value);
	}

	public static boolean isValidIpV4(final String ipv4) {
		return IPV4_PATTERN.matcher(ipv4).matches();
	}

	public static boolean isValidIpV6(final String ipv6) {
		return IPV6_PATTERN.matcher(ipv6).matches();
	}

	public static boolean isValidUri(final String uri) {
		try {
			new URL(uri).toURI();
			return true;
		} catch (final Exception e) {
			return false;
		}
	}
}
