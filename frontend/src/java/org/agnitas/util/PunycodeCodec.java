/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.net.IDN;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunycodeCodec {
	private static final Pattern DOMAIN_FROM_LINK_PATTERN = Pattern.compile("^([^:]+://)([^/?;#]*)([/?;#].*)?$");

	public static String encodeDomainName(String domainName) throws Exception {
		if (domainName == null) {
			return null;
		} else {
			return IDN.toASCII(domainName);
		}
	}

	/**
	 * Takes given link and Punycode-encodes its domain name.
	 * 
	 * @param domainNameToDecode
	 *            URL to encode
	 * 
	 * @return URL with Punycode-encoded domain name
	 * 
	 * @throws Exception
	 *             on errors Punycode-encoding link
	 */
	public static String decodeDomainName(String domainNameToDecode) throws Exception {
		if (domainNameToDecode == null) {
			return null;
		} else {
			return IDN.toUnicode(domainNameToDecode);
		}
	}

	public static String encodeEmailAdress(String email) throws Exception {
		if (email == null) {
			return null;
		} else if (!AgnUtils.isEmailValid(email)) {
			throw new Exception("Invalid email address");
		} else {
			int mailDelimiterIndex = email.trim().indexOf('@');
			if (mailDelimiterIndex < 0) {
				throw new Exception("Invalid email address");
			} else {
				String localPart = email.substring(0, mailDelimiterIndex);
				String domainName = email.substring(mailDelimiterIndex + 1);
				StringBuilder encodedEmail = new StringBuilder();
				encodedEmail.append(localPart);
				encodedEmail.append('@');
				encodedEmail.append(encodeDomainName(domainName));
	
				return encodedEmail.toString();
			}
		}
	}

	public static String decodeEmailAdress(String emailToDecode) throws Exception {
		if (emailToDecode == null) {
			return null;
		} else {
			int mailDelimiterIndex = emailToDecode.trim().indexOf('@');
			if (mailDelimiterIndex < 0) {
				throw new Exception("Invalid email address");
			} else {
				String localPart = emailToDecode.substring(0, mailDelimiterIndex);
				String domainName = emailToDecode.substring(mailDelimiterIndex + 1);
				StringBuilder decodedEmail = new StringBuilder();
				decodedEmail.append(localPart);
				decodedEmail.append('@');
				decodedEmail.append(decodeDomainName(domainName));
	
				return decodedEmail.toString();
			}
		}
	}

	public static final String encodeDomainInLink(final String fullUrl) throws Exception {
		final Matcher matcher = DOMAIN_FROM_LINK_PATTERN.matcher(fullUrl);

		if (matcher.matches()) {
			final StringBuffer buffer = new StringBuffer();
			// Schema-part including "://"
			if (matcher.group(1) != null) {
				buffer.append(matcher.group(1));
			}

			// Domain name without trailing "/"
			buffer.append(encodeDomainName(matcher.group(2)));

			// Everything after the domain name
			if (matcher.group(3) != null) {
				buffer.append(matcher.group(3));
			}

			return buffer.toString();
		} else {
			return fullUrl;
		}
	}
}
