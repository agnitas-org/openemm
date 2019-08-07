/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

/**
 * Exception indicating a required but missing <i>agnDVALUE</i> tag.
 */
public class MissingValueTagException extends DynTagException {

	/** Serial version UID. */
	private static final long serialVersionUID = -61952916679619136L;

	/**
	 * Create a new exception.
	 * 
	 * @param lineNumber line number in which the error occurred
	 * @param tag name of erroneous tag
	 */
	public MissingValueTagException( int lineNumber, String tag) {
		super( lineNumber, tag, "Missing end tag for '" + tag + "' at line " + lineNumber);
	}
}
