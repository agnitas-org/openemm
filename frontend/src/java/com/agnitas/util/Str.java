/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.IDN;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of simple string convenient methods
 * with no further dependency
 */
public class Str {
	static private Map <String, String> homeDirectories = null;
	/**
	 * Converts a string to a boolean according to its content
	 *
	 * @param s    the string to parse
	 * @param dflt the default value
	 * @return the boolean value, if the string could be parsed, dflt otherwise
	 */
	public static boolean atob(String s, boolean dflt) {
		if (s != null) {
			if (s.equalsIgnoreCase("true") ||
			    s.equalsIgnoreCase("yes") ||
			    s.equalsIgnoreCase("on") ||
			    s.equalsIgnoreCase("enabled") ||
			    s.equals("+") ||
			    s.equals("1")) {
				return true;
			} else if (s.equalsIgnoreCase("false") ||
				   s.equalsIgnoreCase("no") ||
				   s.equalsIgnoreCase("off") ||
				   s.equalsIgnoreCase("disabled") ||
				   s.equals("-") ||
				   s.equals("0")) {
				return false;
			}
		}
		return dflt;
	}
	public static boolean atob (String s) {
		return atob (s, false);
	}
	public static String btoa (boolean b) {
		return b ? "true" : "false";
	}

	/**
	 * Converts a string to an Integer
	 *
	 * @param s    the string to parse
	 * @param dflt the default value
	 * @return the parsed value, if parsable, dflt otherwise
	 */
	public static int atoi(String s, int dflt) {
		int rc;

		try {
			rc = Integer.parseInt(s);
		} catch (Exception e) {
			rc = dflt;
		}
		return rc;
	}
	public static int atoi (String s) {
		return atoi (s, 0);
	}

	/**
	 * Converts a string to an Long
	 *
	 * @param s    the string to parse
	 * @param dflt the default value
	 * @return the parsed value, if parsable, dflt otherwise
	 */
	public static long atol(String s, long dflt) {
		long rc;

		try {
			rc = Long.parseLong(s);
		} catch (Exception e) {
			rc = dflt;
		}
		return rc;
	}
	public static long atol(String s) {
		return atol (s, 0);
	}

	/**
	 * Converts a string to an double (float)
	 *
	 * @param s    the string to parse
	 * @param dflt the default value
	 * @return the parsed value, if parsable, dflt otherwise
	 */
	public static double atof(String s, double dflt) {
		double rc;

		try {
			rc = Double.parseDouble(s);
		} catch (Exception e) {
			rc = dflt;
		}
		return rc;
	}

	/**
	 * simple string template processor where a missing referenced token
	 * will result in a RuntimeException. The operation system enviromen
	 * will be automatically be accessable
	 *
	 * @param s    the string to process
	 * @param parm a list of pairs variable/value to be used for replacement
	 * @return the processed string
	 * @throws RuntimeException
	 */
	private static Pattern FIND_PLACEHOLDER = Pattern.compile("\\$\\$|\\$(\\{[^}]+\\}|[a-z_][a-z0-9_]*)", Pattern.MULTILINE | Pattern.DOTALL);
	public static String fill(String s, Map<String, String> variables) {
		if (s == null) {
			return s;
		}
		
		StringBuffer result = new StringBuffer();
		Matcher m = FIND_PLACEHOLDER.matcher(s);
		Map<String, String> env = System.getenv();
		int pos = 0;
		int start;
		String placeholder, replace;

		while (m.find(pos)) {
			start = m.start();
			if (start > pos) {
				result.append(s, pos, start);
			}
			placeholder = m.group(1);
			if (placeholder == null) {
				replace = "$";
			} else {
				if (placeholder.startsWith("{") && placeholder.endsWith("}")) {
					placeholder = placeholder.substring(1, placeholder.length() - 1);
				}
				replace = variables.get(placeholder);
				if (replace == null) {
					replace = env.get(placeholder);
				}
			}
			result.append(replace != null ? replace : m.group());
			pos = m.end();
		}
		if (pos < s.length()) {
			result.append(s.substring(pos));
		}
		return result.toString();
	}

	public static String fill(String s, Object... param) {
		if (s == null) {
			return s;
		}
		
		Map<String, String> p = new HashMap<>();

		for (int n = 0; n + 1 < param.length; n += 2) {
			p.put(param[n].toString(), param[n + 1].toString());
		}
		return fill(s, p);
	}

	/**
	 * Create a valid path for this operating system where each element
	 * is treated as a template string. Beside the enviroment, the two
	 * token ${home} is replaced by the system property "user.home"
	 *
	 * @param elements the elements for the path
	 * @return the generated path
	 * @throws RuntimeException
	 */
	public static String makePath(Object... elements) {
		return Arrays.stream(elements)
				.map(element -> (element instanceof String) ?
						fill((String) element, "home", Systemconfig.home, "user", Systemconfig.user) :
						element.toString())
				.map(element -> expand (element))
				.reduce((s, e) -> s + (e.startsWith (File.separator) ? "" : File.separator) + e)
				.orElse(".");
	}
	public static String makePath (String path) {
		return makePath ((Object[]) path.split (File.separator));
	}
	private static synchronized String expand (String element) {
		if ("~".equals (element)) {
			return Systemconfig.home;
		}
		if ((element != null) && (element.startsWith ("~"))) {
			if (homeDirectories == null) {
				try {
					homeDirectories = new HashMap <> ();
					
					File	file = new File ("/etc/passwd");
					if (file.exists () && file.canRead ()) {
						try (FileInputStream fd = new FileInputStream (file)) {
							byte[]	content = new byte[(int) file.length ()];
							
							fd.read (content);
							for (String line : (new String (content, StandardCharsets.UTF_8)).split ("\n")) {
								String[]	pwd = line.split (":");

								if (pwd.length == 7) {
									homeDirectories.put (pwd[0], pwd[5]);
								}
							}
						} catch (IOException e) {
							// do nothing
						}
					}
				} catch (SecurityException e) {
					// do nothing
				}
			}
			return homeDirectories.getOrDefault (element.substring (1), element);
		}
		return element;
	}

	/**
	 * convert an email domain part into its punycode representation, if possible
	 * 
	 * @param domain the domain to convert
	 * @return the converted domain, if convertion had been successful, the unmodified domain otherwise
	 */
	public static String punycodeDomain(String domain) {
		if (domain != null) {
			try {
				return IDN.toASCII(domain);
			} catch (Exception e) {
				// do nothing
			}
		}
		return domain;
	}

	/**
	 * convert an email address into a version with the domain part in punycode, if possible
	 * 
	 * @param email the email to convert
	 * @return the converted email, if possible, otherwise the unmodified email
	 */
	public static String punycodeEMail(String email) {
		if (email != null) {
			int at = email.lastIndexOf('@');

			if (at != -1) {
				return email.substring(0, at + 1) + punycodeDomain(email.substring(at + 1));
			}
		}
		return email;
	}
	
	/**
	 * normalize an email for compare purpose
	 * 
	 * @param email the email address to convert
	 * @param allowUnnormalizedEmails if true, then the local part is unchanged, otherwise it is converted to lower case
	 * @return the converted email address
	 */
	public static String normalizeEMail (String email, boolean allowUnnormalizedEmails) {
		if (email != null) {
			email = email.trim ();
			int at = email.lastIndexOf('@');

			if (at != -1) {
				if (allowUnnormalizedEmails) {
					return email.substring(0, at + 1) + punycodeDomain (email.substring (at + 1).toLowerCase ());
				} else {
					return email.substring(0, at + 1).toLowerCase () + punycodeDomain (email.substring (at + 1).toLowerCase ());
				}
			} else {
				return allowUnnormalizedEmails ? email : email.toLowerCase ();
			}
		}
		return email;
	}
}
