/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

import java.util.Arrays;

/**
 * Indicates, that the type of a code fragment does not match the expected type or types.
 */
public class InvalidTypeException extends FaultyCodeException {

	/** Serial version UID. */
	private static final long serialVersionUID = 5132624607232994179L;

	/** The actual data type. */
	private final DataType actualType;
	
	/** The expected types. */
	private final DataType[] expectedTypes;
	
	/**
	 * Instantiates a new invalid type exception.
	 *
	 * @param fragment the fragment
	 * @param expectedTypes the expected types
	 */
	public InvalidTypeException(CodeLocation location, DataType actualType, DataType... expectedTypes) {
		super(location, "Invalid type. Type is " + actualType + " but expected one of {" + fold(expectedTypes) + "}");
		this.actualType = actualType;
		this.expectedTypes = Arrays.copyOf(expectedTypes, expectedTypes.length);
	}
	
	/**
	 * Converts an array of {@link DataType}s to a comma separated String
	 *
	 * @param dataTypes the data types
	 * @return the string
	 */
	private static String fold(DataType... dataTypes) {
		StringBuffer buf = new StringBuffer();
		
		for(int i = 0; i < dataTypes.length; i++) {
			if(i > 0)
				buf.append(", ");
			buf.append(dataTypes[i]);
		}
		
		return buf.toString();
	}
	
	/**
	 * Returns the actual data type.
	 * 
	 * @return actual data type 
	 */
	public DataType getActualType() {
		return actualType;
	}

	/**
	 * Returns the expected data types.
	 * 
	 * @return expected data types
	 */
	public DataType[] getExpectedTypes() {
		return expectedTypes;
	}

}
