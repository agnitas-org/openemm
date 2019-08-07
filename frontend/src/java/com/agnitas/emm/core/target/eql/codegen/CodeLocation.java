/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

/**
 * Describes location in source code.
 */
public class CodeLocation {

	/** Line number. */
	private final int line;
	
	/** Column number. */
	private final int column;
	
	/**
	 * Creates a new code location.
	 * 
	 * @param line line number
	 * @param column column number
	 */
	public CodeLocation(int line, int column) {
		this.line = line;
		this.column = column;
	}
	
	/**
	 * Returns the line number.
	 * 
	 * @return line number
	 */
	public int getLine() {
		return this.line;
	}
	
	/**
	 * Returns the column number.
	 * 
	 * @return column number
	 */
	public int getColumn() {
		return this.column;
	}
	
}
