/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.utils;

import java.text.BreakIterator;
import java.util.Locale;

public class StringUtils {

    /**
     * Method converts single-line string to the word-wrapped multiline string with the given max-width
     * and max-line-number values (if the string is longer than that - the rest of the string will
     * be thrown out and "..." will be added to the end)
     *
     * @param input - the source string
     * @param width - the max width of one line of text
     * @param maxlines - the max number of lines that result string can contain
     * @param locale - the source string locale; is used to determine the possible places to make word-wraps in the source string
     * @return the string with word wrapping
     */
    public static String wordWrap(String input, int width, int maxlines, Locale locale) {
        if (input == null) {
            return "";
        }
        if (width >= input.length()) {
            return input;
        }
        StringBuilder buf = new StringBuilder(input);
        boolean endOfLine = false;
        int lineStart = 0;
        for (int i = 0; i < buf.length(); i++) {
            if (buf.charAt(i) == '\n') {
                lineStart = i + 1;
                endOfLine = true;
            }
            // handle splitting at width character
            if (i > lineStart + width - 1) {
                if (!endOfLine) {
                    int limit = i - lineStart - 1;
                    BreakIterator breaks = BreakIterator.getLineInstance(locale);
                    breaks.setText(buf.substring(lineStart, i));
                    int end = breaks.last();
                    // if the last character in the search string isn't a space,
                    // we can't split on it. Search for a previous break character
                    if (end == limit + 1) {
                        if (!Character.isWhitespace(buf.charAt(lineStart + end))) {
                            end = breaks.preceding(end - 1);
                        }
                    }
                    // if the last character is a space - replace it with \n
                    if (end != BreakIterator.DONE && end == limit + 1) {
                        buf.replace(lineStart + end, lineStart + end + 1, "\n");
                        lineStart = lineStart + end;
                    }
                    // otherwise - just insert a \n
                    else if (end != BreakIterator.DONE && end != 0) {
                        buf.insert(lineStart + end, '\n');
                        lineStart = lineStart + end + 1;
                    }
                    else {
                        buf.insert(i, '\n');
                        lineStart = i + 1;
                    }
                }
                else {
                    buf.insert(i, '\n');
                    lineStart = i + 1;
                    endOfLine = false;
                }
            }
        }
        // Throw out excess strings
        String result = buf.toString();
        StringBuilder builder = new StringBuilder();
        String[] lines = result.split("\n");
        if (lines != null && lines.length > maxlines) {
            String shortedStr = "...";
            if (lines[maxlines - 1].length() + shortedStr.length() <= width) {
                lines[maxlines - 1] = lines[maxlines - 1] + shortedStr;
            } else {
                lines[maxlines - 1] = lines[maxlines -1].substring(0, width - shortedStr.length()) + shortedStr;
            }
            for (int i = 0; i < maxlines; i++) {
                builder.append(lines[i]).append("\n");
            }
            result = builder.toString();
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * Search for parameters in Mediatype param-string (method is copied from AngUtils)
     * @param paramName the name of param to search
     * @param paramList the string with all params
     * @return the value of param
     */
    public static String findParam(String paramName, String paramList) {
        String result = null;
        if(paramName != null) {
            int posA = paramList.indexOf(paramName+"=\"");
            if(posA != -1) {
                int posB = paramList.indexOf("\",", posA);
                if(posB != -1) {
                    result = paramList.substring(posA+paramName.length()+2, posB);
                    result = result.replace("\\=", "=");
                    result = result.replace("\\\"", "\"");
                    result = result.replace("\\,", ",");
                }
            }
        }
        return result;
    }
	
}
