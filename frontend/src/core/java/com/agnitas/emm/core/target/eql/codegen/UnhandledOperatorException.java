/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

/**
 * This exception indicates an error in the implementation of a parser or code generator.
 * It is thrown, if the code encounters an operator, that is not supported in the current implementation.
 * 
 */
public class UnhandledOperatorException extends CodeGeneratorImplementationException {

	/** Serial version UID. */
	private static final long serialVersionUID = -8929431285118226659L;
	
	/** The operator. */
	private final Object operator;
	
	/**
	 * Instantiates a new unhandled operator exception.
	 *
	 * @param operator the unhandled operator
	 */
	public UnhandledOperatorException(Object operator) {
		super("Unhandled operator: " + operator);
		
		this.operator = operator;
	}
	
	/**
	 * Gets the unhandled operator.
	 *
	 * @return the unhandled operator
	 */
	public Object getOperator() {
		return this.operator;
	}
}
