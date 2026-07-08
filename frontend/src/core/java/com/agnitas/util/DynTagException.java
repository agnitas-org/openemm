/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

/**
 * Exception indicating errors with dynamic tags.
 */
public class DynTagException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = -9022567996889651545L;
	
	/** Line number in which the error occurred. */
	private final int line;
	
	/** Name of erroneous tag. */
	private final String tag;
	
	/**
	 * Create a new exception without error message or cause.
	 * 
	 * @param lineNumber line number in which the error occurred
	 * @param tag name of erroneous tag
	 */
	public DynTagException(int lineNumber, String tag) {
		super();
		
		this.line = lineNumber;
		this.tag = tag;
	}

	/**
	 * Create a new exception with error message and cause.
	 * 
	 * @param lineNumber line number in which the error occurred
	 * @param tag name of erroneous tag
	 * @param message error message
	 * @param cause the cause
	 */
	public DynTagException(int lineNumber, String tag, String message, Throwable cause) {
		super(message, cause);
		
		this.line = lineNumber;
		this.tag = tag;
	}

	/**
	 * Create a new exception with error message.
	 * 
	 * @param lineNumber line number in which the error occurred
	 * @param tag name of erroneous tag
	 * @param message error message
	 */
	public DynTagException(int lineNumber, String tag, String message) {
		super(message);
		
		this.line = lineNumber;
		this.tag = tag;
	}

	/**
	 * Create a new exception with cause.
	 * 
	 * @param lineNumber line number in which the error occurred
	 * @param tag name of erroneous tag
	 * @param cause the cause
	 */
	public DynTagException(int lineNumber, String tag, Throwable cause) {
		super(cause);
		
		this.line = lineNumber;
		this.tag = tag;
	}

	/**
	 * Returns the line number in which the error occurred.
	 * 
	 * @return line number
	 */
	public int getLineNumber() {
		return this.line;
	}
	
	/**
	 * Returns the name of the erroneous tag. 
	 * 
	 * @return name of tag
	 */
	public String getTag() {
		return this.tag;
	}
}
