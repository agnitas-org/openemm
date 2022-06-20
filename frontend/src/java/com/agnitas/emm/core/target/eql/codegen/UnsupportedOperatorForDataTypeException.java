/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

/**
 * Exception indicating, that an operation is not allowed for a specific data type.
 */
public class UnsupportedOperatorForDataTypeException extends FaultyCodeException {

	/** Serial version UID. */
	private static final long serialVersionUID = 5886626509231609174L;
	
	/** The data type. */
	private final DataType type;
	
	/** Operator. */
	private final Object operator;
	
	/**
	 * Instantiates a new unsupported operator for data type exception.
	 *
	 * @param location location of code where the error occurred
	 * @param operator applied operator
	 * @param type used data type
	 */
	public UnsupportedOperatorForDataTypeException(CodeLocation location, Object operator, DataType type) {
		super(location, "Unsupported operator " + operator + " for type " + type);
		
		this.type = type;
		this.operator = operator;
	}

	/**
	 * Returns the data type.
	 *
	 * @return data type
	 */
	public DataType getType() {
		return type;
	}

	/**
	 * Returns the applied operator.
	 *
	 * @return applied operator
	 */
	public Object getOperator() {
		return operator;
	}

}
