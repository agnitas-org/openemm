/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Some useful string operations as static methods
 */
public class StringOps {

	private StringOps() {

	}

	/**
	 * replaces every occurance of `pattern' in `str' with `replace'
	 *
	 * @param str     the source
	 * @param pattern the pattern to replace
	 * @param replace the substition
	 * @return the new string with replacements
	 */
	public static String replace(String str, String pattern, String replace) {
		int s = 0;
		int e;
		StringBuilder result = new StringBuilder();
		while ((e = str.indexOf(pattern, s)) >= 0) {
			result.append(str, s, e);
			result.append(replace);
			s = e + pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();
	}

	/**
	 * fill the left side of the string with `0's until `length' is reached
	 *
	 * @param text   the source
	 * @param length the length to extend the string to
	 * @return the filled string
	 */
	public static String format_number(String text, int length) {
		int textLength = text.length();

		if (textLength >= length) {
			return text;
		}
		String result = text;
		for (int i = textLength; i != length; i++) {
			result = "0" + result;
		}
		return result;
	}

	public static String format_number(long number, int length) {
		return format_number(Long.toString(number), length);
	}

	/**
	 * fill the left side of a string representation of a number with `0's until
	 * length is reached
	 *
	 * @param nr     the source
	 * @param length the length to extend the string to
	 * @return the filled string
	 */
	public static String format_number(int nr, int length) {
		return format_number(Integer.toString(nr), length);
	}

	/**
	 * Split a comma separated string into its elements
	 *
	 * @param str the input string
	 * @return the filled vector
	 */
	public static Vector<String> splitString(String str) {
		return splitString(null, str);
	}

	/**
	 * Split a comma separated string into its elements
	 *
	 * @param v   optional existing vector to append to
	 * @param str the input string
	 * @return the filled vector
	 */
	public static Vector<String> splitString(Vector<String> v, String str) {
		if (v == null) {
			v = new Vector<>();
		}
		if (StringUtils.isNotEmpty(str)) {
			for (String part : str.split(",")) {
				v.add(part.trim());
			}
		}

		return v;
	}

	/**
	 * Simple formating for a date
	 *
	 * @param date the source
	 * @return the formated date as string
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy:HH:mm:ss");

		return fmt.format(date);
	}

	/**
	 * Format a string to its SQL representation
	 *
	 * @param str the source
	 * @return the SQL conform string
	 */
	public static String sqlString(String str) {
		StringBuilder r = new StringBuilder(str.length() + 8);

		r.append('\'');
		for (int n = 0; n < str.length(); ++n) {
			char ch = str.charAt(n);

			r.append(ch);
			if (ch == '\'') {
				r.append(ch);
			}
		}
		r.append('\'');
		return r.toString();
	}

	/**
	 * Transform an SQL representation to a string
	 *
	 * @param str the source
	 * @return the stripped off version
	 */
	public static String unSqlString(String str) {
		int start;
		int end;

		start = 0;
		end = str.length();
		if ((end > 0) && (str.charAt(0) == '\'') && (str.charAt(end - 1) == '\'')) {
			++start;
			--end;

			StringBuffer r = new StringBuffer(end - start + 1);
			for (int n = start; n < end; ++n) {
				char ch = str.charAt(n);

				r.append(ch);
				if ((ch == '\'') && (n + 1 < end) && (str.charAt(n + 1) == '\'')) {
					++n;
				}
			}
			str = r.toString();
		}
		return str;
	}

	/**
	 * convert old style <agn ...> and </agn ...> to new style [agn ...] and
	 * [/agn ...]
	 *
	 * @param in the source
	 * @return the converted string
	 */
	public static String convertOld2New(String in) {
		int ilen = in.length();
		StringBuilder out = new StringBuilder(ilen);

		int n;
		int cur;
		int pos;

		cur = 0;
		while ((cur < ilen) && ((n = in.indexOf('<', cur)) != -1) && (n + 5 < ilen)) {
			out.append(in, cur, n);
			pos = n++;
			if (in.substring(n, n + 3).equals("agn") || in.substring(n, n + 4).equals("/agn")) {
				char quote = '\0';
				char ch;

				out.append('[');
				while (n < ilen) {
					ch = in.charAt(n);
					if (quote == '\0') {
						if (ch == '>') {
							break;
						}
						if ((ch == '"') || (ch == '\'')) {
							quote = ch;
						}
					} else if (quote == ch) {
						quote = '\0';
					}
					out.append(ch);
					++n;
				}
				if (n < ilen) {
					out.append(']');
				}
				cur = n + 1;
			} else {
				out.append(in.charAt(pos));
				cur = pos + 1;
			}
		}
		if (cur < ilen) {
			out.append(in.substring(cur));
		}
		return out.toString();
	}

	/**
	 * Removed HTML entities and tags from the input
	 *
	 * @param inputString input string
	 * @return the HTML-cleared string
	 */
	private static Pattern FINDHTML = Pattern.compile("<[ \t]*[!/?%a-z][^>]*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	public static String removeHTMLTagsAndEntities(String inputString) {
		if (StringUtils.isEmpty(inputString)) {
			return inputString;
		} else {
			Matcher regexMatcher = FINDHTML.matcher(inputString);
			boolean hasNextMatch = regexMatcher.find();
			if (!hasNextMatch) {
				return StringEscapeUtils.unescapeHtml4(inputString);
			} else {
				StringBuilder returnValue = new StringBuilder(inputString.length());

				int endOfPreviousMatchingString = 0;

				while (hasNextMatch) {
					int matchingStringStart = regexMatcher.start();

					if (endOfPreviousMatchingString < matchingStringStart) {
						returnValue.append(inputString, endOfPreviousMatchingString, matchingStringStart);
					}
					endOfPreviousMatchingString = regexMatcher.end();
					hasNextMatch = regexMatcher.find();
				}

				if (endOfPreviousMatchingString < inputString.length()) {
					returnValue.append(inputString.substring(endOfPreviousMatchingString));
				}

				return StringEscapeUtils.unescapeHtml4(returnValue.toString());
			}
		}
	}

	public static String htmlEscape (String source) {
		return source == null ? source : StringEscapeUtils.escapeHtml4 (source);
	}

	public static String htmlUnescape (String source) {
		return source == null ? source : StringEscapeUtils.unescapeHtml4 (source);
	}
}
