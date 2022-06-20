/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;

/**
 * Generic class to format values retrieved from the database
 * for different data types
 */
public class Format {
	private String format;
	private String encoders;
	private Locale locale;
	private TimeZone timeZone;
	private NumberFormat numberFormater;
	private SimpleDateFormat dateFormater;
	private String error;
	private List<Coder> encodeChain;

	/**
	 * Constructor
	 *
	 * @param nFormat   the format string for the value, must be related to the data type
	 * @param nEncoders a comman seprated list of output encoders
	 * @param nLocale   a locale instance for data types that obey the locale
	 * @param nTimeZone the timezone to convert a date type value to
	 */
	public Format(String nFormat, String nEncoders, Locale nLocale, TimeZone nTimeZone) {
		format = nFormat;
		encoders = nEncoders;
		locale = nLocale;
		timeZone = nTimeZone;
		numberFormater = null;
		dateFormater = null;
		error = null;
		encodeChain = null;
		parseEncoders();
	}

	public String format(double val) {
		String rc = null;

		if ((format != null) && validNumberFormat()) {
			rc = numberFormater.format(val);
		}
		return rc;
	}

	public String format(long val) {
		String rc = null;

		if ((format != null) && validNumberFormat()) {
			rc = numberFormater.format(val);
		}
		return rc;
	}

	public String format(String val) {
		return val;
	}

	public String format(Date val) {
		String rc = null;

		if ((format != null) && validDateFormater()) {
			if (val != null) {
				rc = dateFormater.format(val);
			}
		}
		return rc;
	}

	static private Charset defaultCharset = StandardCharsets.UTF_8;

	/**
	 * encode a string according to the defined encoders
	 * as passed to the constructor. Currently supported
	 * encoders are:
	 * - hex/hexlower: convert to lowercase hex representation
	 * - hexupper:     convert to uppercase hex representation
	 * - url:          convert to URL conform representation
	 * - <digest>      create a digest for the value
	 * digestes are:   sha1, sha256, sha384, sha512
	 *
	 * @param input the input string to process
	 * @return the processed string
	 */
	public String encode(String input) {
		if ((input == null) || (encodeChain == null)) {
			return input;
		}

		String s = input;
		byte[] content = null;
		Charset charset = defaultCharset;
		Charset ncharset;

		for (Coder coder : encodeChain) {
			if ((ncharset = coder.getCharset()) != null) {
				charset = ncharset;
				continue;
			}

			if (content == null) {
				if (s == null) {
					error = coder.getClass().getName() + ": input \"" + s + "\" can not be converted to binary (Nullpointer) using charset " + charset.displayName();
					break;
				} else {
					try {
						content = s.getBytes(charset);
					} catch (Exception e) {
						error = coder.getClass().getName() + ": input \"" + s + "\" can not be converted to binary (" + e.toString() + ") using charset " + charset.displayName();
						break;
					}
				}
			}
			try {
				content = coder.codeBinary(content);
				s = null;
			} catch (Exception e1) {
				try {
					s = coder.codeString(content);
				} catch (Exception e2) {
					error = coder.getClass().getName() + ": input \"" + input + "\" can not converted to binary (" + e1.toString() + ") nor string (" + e2.toString() + ")";
					s = null;
					break;
				}
				content = null;
			}
			if ((s == null) && (content == null)) {
				if (error == null) {
					error = coder.getClass().getName() + ": conversion leads to null values";
				}
				break;
			}
		}
		if ((s == null) && (content != null)) {
			Coder coder = encodeChain.get(encodeChain.size() - 1);
			try {
				s = coder.codeStringDefault(content);
			} catch (Exception e) {
				error = coder.getClass().getName() + ": failed to final convert to string (" + e.toString() + ")";
			}
		}
		return s;
	}

	public String error() {
		return error;
	}

	private boolean validNumberFormat() {
		if (numberFormater == null) {
			if (locale != null) {
				numberFormater = NumberFormat.getInstance(locale);
			} else {
				numberFormater = NumberFormat.getInstance();
			}
			if (numberFormater instanceof DecimalFormat) {
				try {
					((DecimalFormat) numberFormater).applyPattern(format);
				} catch (IllegalArgumentException e) {
					error = "Invalid format (" + e.toString() + ") found: " + format;
					numberFormater = null;
				}
			} else {
				error = "Format leads into an unexpected class \"" + numberFormater.getClass().getName() + "\": " + format;
				numberFormater = null;
			}
			if (numberFormater == null) {
				format = null;
			}
		}
		return numberFormater != null;
	}

	private boolean validDateFormater() {
		if (dateFormater == null) {
			try {
				if (locale != null) {
					dateFormater = new SimpleDateFormat(format, locale);
				} else {
					dateFormater = new SimpleDateFormat(format);
				}
			} catch (IllegalArgumentException e) {
				error = "Invalid format (" + e.toString() + ") found: " + format;
				dateFormater = null;
			}
			if ((dateFormater != null) && (timeZone != null)) {
				dateFormater.setTimeZone(timeZone);
			}
			if (dateFormater == null) {
				format = null;
			}
		}
		return dateFormater != null;
	}

	class Coder {
		private Charset charset = null;

		public Charset getCharset() {
			return charset;
		}

		public void setCharset(Charset nCharset) {
			charset = nCharset;
		}

		public byte[] codeBinary(byte[] input) throws Exception {
			throw new Exception("no bindary coding supported");
		}

		public String codeString(byte[] input) throws Exception {
			return codeStringDefault(input);
		}

		public String codeStringDefault(byte[] input) throws Exception {
			return codeStringHexLower(input);
		}

		char[] lower = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

		public String codeStringHexLower(byte[] input) throws Exception {
			return codeStringHex(input, lower);
		}

		char[] upper = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

		public String codeStringHexUpper(byte[] input) throws Exception {
			return codeStringHex(input, upper);
		}

		private String codeStringHex(byte[] input, char[] hex) throws Exception {
			StringBuffer output = new StringBuffer(input.length * 2);

			for (int n = 0; n < input.length; ++n) {
				output.append(hex[(input[n] >> 4) & 0xf]);
				output.append(hex[input[n] & 0xf]);
			}
			return output.toString();
		}
	}

	class CoderHexLower extends Coder {
		@Override
		public String codeString(byte[] input) throws Exception {
			return codeStringHexLower(input);
		}
	}

	class CoderHexUpper extends Coder {
		@Override
		public String codeString(byte[] input) throws Exception {
			return codeStringHexUpper(input);
		}
	}

	class CodeURL extends Coder {
		char[] safe = { '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '-', '.', '\0', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '\0', '\0', '\0', '\0', '\0', '\0', '\0', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '\0', '\0', '\0', '\0', '_', '\0', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '\0', '\0', '\0', '~', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0' };

		@Override
		public String codeString(byte[] input) throws Exception {
			StringBuffer output = new StringBuffer((input.length * 5) / 4);

			for (byte b : input) {
				if (safe[b] != '\0') {
					output.append(safe[b]);
				} else {
					output.append('%');
					output.append(upper[(b >> 4) & 0xf]);
					output.append(upper[b & 0xf]);
				}
			}
			return output.toString();
		}
	}

	class CoderDigest extends Coder {
		private MessageDigest md;

		public CoderDigest(String digest) throws Exception {
			md = MessageDigest.getInstance(digest);
		}

		@Override
		public byte[] codeBinary(byte[] input) throws Exception {
			md.reset();
			return md.digest(input);
		}
	}

	private static Map<String, String> digestMap = null;

	static {
		digestMap = new HashMap<>();
		digestMap.put("sha1", "sha-1");
		digestMap.put("sha256", "sha-256");
		digestMap.put("sha384", "sha-384");
		digestMap.put("sha512", "sha-512");
	}

	private void parseEncoders() {
		encodeChain = null;

		String[] coders;
		SortedMap<String, Charset> charsets = Charset.availableCharsets();

		if ((encoders != null) && ((coders = encoders.toLowerCase().split(", *")) != null) && (coders.length > 0)) {
			encodeChain = new ArrayList<>(coders.length);

			for (String coder : coders) {
				Coder c;

				switch (coder) {
					case "hex":
					case "hexlower":
						c = new CoderHexLower();
						break;
					case "hexupper":
						c = new CoderHexUpper();
						break;
					case "url":
						c = new CodeURL();
						break;
					default:
						try {
							if (digestMap.containsKey(coder)) {
								coder = digestMap.get(coder);
							}
							c = new CoderDigest(coder);
						} catch (Exception e) {
							if (charsets.containsKey(coder) && charsets.get(coder).canEncode()) {
								c = new Coder();
								c.setCharset(charsets.get(coder));
							} else {
								error = coder + ": unknown encoder (" + e.toString() + ")";
								c = null;
							}
						}
						break;
				}
				if (c == null) {
					break;
				}
				encodeChain.add(c);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new Exception("Usage: Format coders string");
		}
		String coders = args[0];
		String input = args[1];

		Format f = new Format(null, coders, null, null);
		System.out.println("'" + input + "' --> '" + f.encode(input) + "'");
	}
}
