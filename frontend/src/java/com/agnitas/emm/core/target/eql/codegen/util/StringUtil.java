/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.util;

/**
 * Utility class dealing with Strings.
 */
public class StringUtil {

	/** Escape character used by EQL. */
	public static final char EQL_ESCAPE_CHAR = '\\';
	
	/**
	 * Replaces escaped characters.
	 * 
	 * @param str String containing escape characters
	 * 
	 * @return String without escape characters
	 */
	public static String replaceEscapedChars(final String str) {
		final StringBuffer buffer = new StringBuffer();

		String remnant = str;
		int index = 0;
		
		while((index = remnant.indexOf(EQL_ESCAPE_CHAR)) != -1) {
			String prefix = remnant.substring(0, index);
			
			if(index + 2 <= remnant.length()) {
				String escaped = remnant.substring(index + 1, index + 2);
				remnant = remnant.substring(index + 2);
				
				buffer.append(prefix);
				buffer.append(escaped);
			} else {
				/*
				 * If escape char is the last character in string, consider it as a regular character and preserve it.
				 * 
				 * (This should never occur due to the EQL grammar specification, but for just in case ;)
				 */
				buffer.append(remnant);
				remnant = "";
			}
		}
		
		buffer.append(remnant);
		
		return buffer.toString();
	}

	/**
	 * Converts the LIKE-pattern from EQL-style to SQL-style.
	 * 
	 * @param string EQL-styled LIKE pattern
	 * @param likeEscapeChar escape character to be used in SQL-styled LIKE pattern
	 * 
	 * @return SQL-styled LIKE pattern
	 */
	public static String convertEqlToSqlLikePattern(final String string, final char likeEscapeChar) {
		final StringBuffer buffer = new StringBuffer();
		
		String remnant = string;
		int index;
		
		while((index = findFirstOf(remnant, '%', '_', '*', '?', likeEscapeChar)) != -1) {
			String prefix = remnant.substring(0, index);
			char symbol = remnant.substring(index, index + 1).charAt(0);
			remnant = remnant.substring(index + 1);
			
			buffer.append(prefix);
			
			if(symbol == likeEscapeChar) {
				// Symbol is used as escape char, so we need to escape it too.
				buffer.append(likeEscapeChar);
				buffer.append(symbol);
			} else {
				switch(symbol) {
				case '%':
				case '_':
					// Symbols have special meaning in SQL (-> wildcards), so we need to escape them.
					// buffer.append(likeEscapeChar); -> GWUA-4324 -> do not escape them
					buffer.append(symbol);
					break;
					
				case '*':
					buffer.append('%');
					break;
					
				case '?':
					buffer.append('_');
					break;
				default:
					break;
				}
			}
		}
		
		buffer.append(remnant);

		return buffer.toString();
	}

	/**
	 * Converts the string constant (value for CONTAINS or STARTS WITH operator) from EQL-style to SQL-style.
	 *
	 * @param string EQL-styled string constant
	 * @param likeEscapeChar escape character to be used in SQL-styled LIKE pattern
	 *
	 * @return SQL-styled LIKE pattern
	 */
	public static String convertEqlToSqlString(String string, char likeEscapeChar) {
		final StringBuilder builder = new StringBuilder();

		String remnant = string;
		int index;

		while ((index = findFirstOf(remnant, '%', '_', likeEscapeChar)) != -1) {
			builder.append(remnant, 0, index);
			builder.append(likeEscapeChar);
			builder.append(remnant.charAt(index));

			remnant = remnant.substring(index + 1);
		}

		builder.append(remnant);

		return builder.toString();
	}

	/**
	 * Returns the index of first occurrence of one of the given characters in a given String.
	 * -1 is returned, if the String contains none of the given characters
	 * 
	 * @param haystack String
	 * @param needles list of characters to search in given String
	 * 
	 * @return index of the first occurrence of one of the given characters or -1 of none was found
	 */
	private static int findFirstOf(String haystack, char...needles) {
		int minIndex = -1;
		int index;
		
		for(int i = 0; i < needles.length; i++) {
			index = haystack.indexOf(needles[i]);
			
			if(index != -1) {
				if(minIndex == -1 || index < minIndex) {
					minIndex = index;
				}
			}
				
		}
		
		return minIndex;
	}

	/**
	 * Converts a String to an EQL string with escape characters and leading and trailing single quotes.
	 * 
	 * @param string String to convert
	 * 
	 * @return EQL string with escape characters and leading and trailing single quotes
	 */
	public static String makeEqlStringConstant(String string) {
		String remnant = string;
		final StringBuffer buffer = new StringBuffer();
		int index;
		
		buffer.append('\'');
		
		while((index = findFirstOf(remnant, '\'', '\\')) != -1) {
			buffer.append(remnant.substring(0, index));
			buffer.append(EQL_ESCAPE_CHAR);
			buffer.append(remnant.substring(index, index + 1));
			remnant = remnant.substring(index + 1);
		}
		buffer.append(remnant);
		
		buffer.append('\'');

		return buffer.toString();
	}

	/**
	 * Converts the given String to an EQL matching pattern. Since EQL does not support escape characters in matching patterns,
	 * the characters <i>*</i> and <i>?</i> cannot be converted. In such a case, the method throws an {@link IllegalArgumentException}.
	 * 
	 * @param string String to convert to EQL matching pattern
	 * @return EQL matching pattern
	 * 
	 * @throws IllegalArgumentException if the given String contains characters that cannot be converted
	 */
	public static Object makeEqlMatchingPattern(String string) throws IllegalArgumentException {
		String remnant = string;
		final StringBuffer buffer = new StringBuffer();
		int index;
		
		buffer.append('\'');
		
		while((index = findFirstOf(remnant, '\'', '\\', '%', '_', '*', '?')) != -1) {
			char symbol = remnant.substring(index, index + 1).charAt(0);
			buffer.append(remnant.substring(0, index));
			
			switch(symbol) {
			case '\'':
			case '\\':
				buffer.append(EQL_ESCAPE_CHAR);
				buffer.append(symbol);
				break;
				
			case '%':
				buffer.append('*');
				break;
				
			case '_':
				buffer.append('?');
				break;
				
			case '*':
			case '?':
				throw new IllegalArgumentException("Matching pattern contains unconvertible symbol '" + symbol + "'");
				
			default:
				// Nothing to do here.
			}
			
			remnant = remnant.substring(index + 1);
		}
		buffer.append(remnant);
		
		buffer.append('\'');

		return buffer.toString();
	}

	
}
