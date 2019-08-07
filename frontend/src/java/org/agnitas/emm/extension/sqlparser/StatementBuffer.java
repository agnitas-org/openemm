/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.sqlparser;

/**
 * Utility class used by the states of the SQL parser.
 */
class StatementBuffer {
	/** Wrapped StringBuffer.*/
	private StringBuffer buffer;
	
	/**
	 * Creates a new StatementBuffer instance.
	 */
	public StatementBuffer() {
		this.buffer = new StringBuffer();
	}
	
	/**
	 * Appends a char to the buffered statement. Since the parameter is of type int,
	 * nothing happens for values less than zero.
	 * 
	 * @param i character to append
	 */
	public void appendChar( int i) {
		if( i < 0)
			return;
		
		buffer.append( (char) i);
	}
	
	/**
	 * Appends a char to the buffered statement.
	 * 
	 * @param c character to append
	 */
	public void appendChar( char c) {
		buffer.append( c);
	}
	
	@Override
	public String toString() {
		return this.buffer.toString().trim();
	}
	
	/**
	 * Clears the statement buffer. Internally, a new StringBuffer is created.
	 */
	public void clear() {
		this.buffer = new StringBuffer();
	}
}
