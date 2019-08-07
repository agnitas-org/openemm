/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.constants;

public class TableSpecialCharacters {
    /**
     * Line separator which works on Mac, Linux and Windows.
     */
    public static final String CRLF_LINE_SEPARATOR = "\r\n";

    /**
     * Prevent line breaking between words.
     */
    public static final String NON_BREAKING_SPACE = "\u00A0";

    /**
     * Zero width non breaking space for wrapping characters which can be break line.
     */
    public static final String WORD_JOINER = "\u2060";

    public static final String CORNER = "+";

    public static final String HORIZONTAL_BORDER = "-";

    public static final String VERTICAL_BORDER = "|";
}
