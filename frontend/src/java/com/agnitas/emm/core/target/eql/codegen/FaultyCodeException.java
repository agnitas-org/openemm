/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;

/**
 * This sub-class of {@link CodeGeneratorException} indicates a semantic error found
 * during code generation.
 * 
 * An example of error raising this exception are incompatible data types in a relational operation.
 */
public abstract class FaultyCodeException extends CodeGeneratorException {

	/** Serial version UID. */
	private static final long serialVersionUID = 1290565138312218056L;
	
	/** Location of error. */
	private final CodeLocation location;
	
	/** Reason of error. */
	private final String reason;
	
	/**
	 * Creates a new exception with given error data. Code location of error is taken from given AST node.
	 * 
	 * @param faultyNode node with error
	 * @param reason reason of error
	 */
	public FaultyCodeException(final AbstractEqlNode faultyNode, final String reason) {
		super(makeErrorMessage(faultyNode, reason));
		
		this.location = faultyNode.getStartLocation();
		this.reason = reason;
	}
	/**
	 * Creates a new exception with given error data. Code location of error is taken from given AST node.
	 * 
	 * @param faultyNode node with error
	 * @param reason reason of error
	 */
	public FaultyCodeException(final AbstractEqlNode faultyNode, final String reason, Throwable cause) {
		super(makeErrorMessage(faultyNode, reason), cause);
		
		this.location = faultyNode.getStartLocation();
		this.reason = reason;
	}
	
	/**
	 * Creates a new exception with given error data.
	 * 
	 * @param codeLocation location of error
	 * @param reason reason of error
	 */
	public FaultyCodeException(final CodeLocation codeLocation, final String reason) {
		super(makeErrorMessage(codeLocation, reason));
		
		this.location = codeLocation;
		this.reason = reason;
	}
	
	/**
	 * Creates a new exception with given error data.
	 * 
	 * @param codeLocation location of error
	 * @param reason reason of error
	 */
	public FaultyCodeException(final CodeLocation codeLocation, final String reason, Throwable cause) {
		super(makeErrorMessage(codeLocation, reason), cause);
		
		this.location = codeLocation;
		this.reason = reason;
	}
	
	private static String makeErrorMessage(AbstractEqlNode node, String reason) {
		return makeErrorMessage(node.getStartLocation(), reason);
	}
	
	private static String makeErrorMessage(CodeLocation codeLocation, String reason) {
		return String.format("Error in line %d, column %d: %s", codeLocation.getLine(), codeLocation.getColumn(), reason);
	}

	/**
	 * Returns location of error.
	 * 
	 * @return location of error
	 */
	public CodeLocation getCodeLocation() {
		return this.location;
	}

	/**
	 * Returns reason of error.
	 * 
	 * @return reason of error
	 */
	public String getReason() {
		return reason;
	}
	
	
}
