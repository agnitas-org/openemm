/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunycodeCodec {
	
	private static final Pattern DOMAIN_FROM_LINK_PATTERN = Pattern.compile("^([^:]+://)([^/?;#]*)([/?;#].*)?$");
	
	private final static int TMIN = 1;
	private final static int TMAX = 26;
	private final static int BASE = 36;
	private final static int INITIAL_N = 128;
	private final static int INITIAL_BIAS = 72;
	private final static int DAMP = 700;
	private final static int SKEW = 38;
	private final static char DELIMITER = '-';
	private final static String ENCODED_DOMAINNAME_PREFIX = "xn--";

	/**
	 * Encode a string in Unicode
	 */
	public static String encode(String stringToEncode) throws Exception {
		if (stringToEncode == null) {
			return null;
		}

		int n = INITIAL_N;
		int delta = 0;
		int bias = INITIAL_BIAS;

		StringBuffer output = new StringBuffer();
		for (char character : stringToEncode.toCharArray()) {
			if (isSimpleCharacter(character)) {
				output.append(character);
			}
		}

		int baseOutputLength = output.length();
		if (baseOutputLength > 0 && baseOutputLength < stringToEncode.length()) {
			output.append(DELIMITER);
		}

		int h = baseOutputLength;
		while (h < stringToEncode.length()) {
			int m = Integer.MAX_VALUE;

			for (int character : stringToEncode.toCharArray()) {
				if (character >= n && character < m) {
					m = character;
				}
			}

			if (m - n > (Integer.MAX_VALUE - delta) / (h + 1)) {
				throw new Exception("Punycode overflow");
			}
			delta = delta + (m - n) * (h + 1);
			n = m;

			for (char character : stringToEncode.toCharArray()) {
				if (character < n) {
					delta++;
					if (0 == delta) {
						throw new Exception("Punycode overflow");
					}
				}
				if (character == n) {
					int q = delta;

					for (int k = BASE;; k += BASE) {
						int t;
						if (k <= bias) {
							t = TMIN;
						} else if (k >= bias + TMAX) {
							t = TMAX;
						} else {
							t = k - bias;
						}
						if (q < t) {
							break;
						}
						output.append((char) punycodeDigitToCodePoint(t + (q - t) % (BASE - t)));
						q = (q - t) / (BASE - t);
					}

					output.append((char) punycodeDigitToCodePoint(q));
					bias = adapt(delta, h + 1, h == baseOutputLength);
					delta = 0;
					h++;
				}
			}

			delta++;
			n++;
		}

		return output.toString();
	}

	/**
	 * Decode a punycoded string
	 */
	public static String decode(String punycodeString) throws Exception {
		if (punycodeString == null) {
			return null;
		}

		int n = INITIAL_N;
		int i = 0;
		int bias = INITIAL_BIAS;
		StringBuffer output = new StringBuffer();

		int d = punycodeString.lastIndexOf(DELIMITER);
		if (d > 0) {
			for (int j = 0; j < d; j++) {
				char c = punycodeString.charAt(j);
				if (!isSimpleCharacter(c)) {
					throw new Exception("Invalid input for decoding punycode");
				}
				output.append(c);
			}
			d++;
		} else {
			d = 0;
		}

		while (d < punycodeString.length()) {
			int oldi = i;
			int w = 1;

			for (int k = BASE;; k += BASE) {
				if (d == punycodeString.length()) {
					throw new Exception("Invalid input for decoding punycode");
				}
				int c = punycodeString.charAt(d++);
				int digit = codePointToPunycodeDigit(c);
				if (digit > (Integer.MAX_VALUE - i) / w) {
					throw new Exception("Punycode overflow");
				}

				i = i + digit * w;

				int t;
				if (k <= bias) {
					t = TMIN;
				} else if (k >= bias + TMAX) {
					t = TMAX;
				} else {
					t = k - bias;
				}
				if (digit < t) {
					break;
				}
				w = w * (BASE - t);
			}

			bias = adapt(i - oldi, output.length() + 1, oldi == 0);

			if (i / (output.length() + 1) > Integer.MAX_VALUE - n) {
				throw new Exception("Punycode overflow");
			}

			n = n + i / (output.length() + 1);
			i = i % (output.length() + 1);
			output.insert(i, (char) n);
			i++;
		}

		return output.toString();
	}

	private final static int adapt(int delta, int numpoints, boolean first) {
		if (first) {
			delta = delta / DAMP;
		} else {
			delta = delta / 2;
		}

		delta = delta + (delta / numpoints);

		int k = 0;
		while (delta > ((BASE - TMIN) * TMAX) / 2) {
			delta = delta / (BASE - TMIN);
			k = k + BASE;
		}

		return k + ((BASE - TMIN + 1) * delta) / (delta + SKEW);
	}

	private final static boolean isSimpleCharacter(char character) {
		return character < 0x80;
	}

	private final static int punycodeDigitToCodePoint(int punycodeDigit) throws Exception {
		if (punycodeDigit < 26) {
			return punycodeDigit + 'a';
		} else if (punycodeDigit < 36) {
			return punycodeDigit - 26 + '0';
		} else {
			throw new Exception("Invalid input for decoding punycode");
		}
	}

	private final static int codePointToPunycodeDigit(int codepoint) throws Exception {
		if (codepoint - '0' < 10) {
			return codepoint - '0' + 26;
		} else if (codepoint - 'a' < 26) {
			return codepoint - 'a';
		} else {
			throw new Exception("Invalid input for decoding punycode");
		}
	}

	public static String encodeDomainName(String domainName) throws Exception {
		if (domainName == null) {
			return null;
		}

		String[] domainNameParts = domainName.toLowerCase().trim().split("\\.");
		StringBuilder punycodedDomainName = new StringBuilder(domainName.length());
		for (String domainNamePart : domainNameParts) {
			if (punycodedDomainName.length() > 0) {
				punycodedDomainName.append('.');
			}

			String punycodedDomainNamePart = encode(domainNamePart);
			if (!punycodedDomainNamePart.equals(domainNamePart)) {
				punycodedDomainName.append(ENCODED_DOMAINNAME_PREFIX);
				punycodedDomainName.append(punycodedDomainNamePart);
			} else {
				punycodedDomainName.append(domainNamePart);
			}
		}
		return punycodedDomainName.toString();
	}

	/**
	 * Takes given link and Punycode-encodes its domain name.
	 * 
	 * @param domainNameToDecode URL to encode
	 * 
	 * @return URL with Punycode-encoded domain name
	 * 
	 * @throws Exception on errors Punycode-encoding link
	 */
	public static String decodeDomainName(String domainNameToDecode) throws Exception {
		if (domainNameToDecode == null) {
			return null;
		}

		String[] domainNameParts = domainNameToDecode.toLowerCase().trim().split("\\.");
		StringBuilder decodedDomainName = new StringBuilder();
		for (String domainNamePart : domainNameParts) {
			if (decodedDomainName.length() > 0) {
				decodedDomainName.append('.');
			}

			if (domainNamePart.startsWith(ENCODED_DOMAINNAME_PREFIX)) {
				domainNamePart = domainNamePart.substring(ENCODED_DOMAINNAME_PREFIX.length());
				String punycodedDomainNamePart = decode(domainNamePart);
				if (!punycodedDomainNamePart.equals(domainNamePart)) {
					decodedDomainName.append(punycodedDomainNamePart);
				} else {
					decodedDomainName.append(domainNamePart);
				}
			} else {
				decodedDomainName.append(domainNamePart);
			}
		}
		return decodedDomainName.toString();
	}

	public static String encodeEmailAdress(String email) throws Exception {
		if (email == null) {
			return null;
		}

		int mailDelimiterIndex = email.trim().indexOf('@');
		if (mailDelimiterIndex < 0) {
			throw new Exception("Invalid email address");
		}

		String localPart = email.substring(0, mailDelimiterIndex);
		String domainName = email.substring(mailDelimiterIndex + 1);
		StringBuilder encodedEmail = new StringBuilder();
		encodedEmail.append(localPart);
		encodedEmail.append('@');
		encodedEmail.append(encodeDomainName(domainName));

		return encodedEmail.toString();
	}

	public static String decodeEmailAdress(String emailToDecode) throws Exception {
		if (emailToDecode == null) {
			return null;
		}

		int mailDelimiterIndex = emailToDecode.trim().indexOf('@');
		if (mailDelimiterIndex < 0) {
			throw new Exception("Invalid email address");
		}

		String localPart = emailToDecode.substring(0, mailDelimiterIndex);
		String domainName = emailToDecode.substring(mailDelimiterIndex + 1);
		StringBuilder decodedEmail = new StringBuilder();
		decodedEmail.append(localPart);
		decodedEmail.append('@');
		decodedEmail.append(decodeDomainName(domainName));

		return decodedEmail.toString();
	}
	

	public static final String encodeDomainInLink(final String fullUrl) throws Exception {
		final Matcher matcher = DOMAIN_FROM_LINK_PATTERN.matcher(fullUrl);
		
		if(matcher.matches()) {
			final StringBuffer buffer = new StringBuffer();
			// Schema-part including "://"
			if(matcher.group(1) != null) {
				buffer.append(matcher.group(1)); 							
			}
			
			// Domain name without trailing "/"
			buffer.append(encodeDomainName(matcher.group(2)));		
			
			// Everything after the domain name
			if(matcher.group(3) != null) {
				buffer.append(matcher.group(3));
			}
			
			return buffer.toString();
		} else {
			return fullUrl;
		}
	}

}
