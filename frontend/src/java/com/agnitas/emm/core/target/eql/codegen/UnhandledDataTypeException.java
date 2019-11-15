/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

/**
 * Exception indicating a data type, that was not handled or cannot be handled during code generation.
 */
public class UnhandledDataTypeException extends CodeGeneratorImplementationException {
	
	/** Serial version UID. */
	private static final long serialVersionUID = 1917720362339773582L;
	
	/** Code fragment containing the error. */
	private final CodeFragment fragment;
	
	/** Unhandled data type. */
	private final DataType datatype;
	
	/**
	 * Creates a new exception.
	 * 
	 * @param fragment code fragment containing the error.
	 * @param datatype unhandled data type
	 */
	public UnhandledDataTypeException(CodeFragment fragment, DataType datatype) {
		super("Unhandled data type " + datatype);
		this.fragment = fragment;
		this.datatype = datatype;
	}

	/**
	 * Returns the code fragment containing the error.
	 * 
	 * @return code fragment containing the error
	 */
	public CodeFragment getFragment() {
		return fragment;
	}

	/**
	 * Returns the unhandled data type.
	 * 
	 * @return unhandled data type
	 */
	public DataType getDatatype() {
		return datatype;
	}
}
