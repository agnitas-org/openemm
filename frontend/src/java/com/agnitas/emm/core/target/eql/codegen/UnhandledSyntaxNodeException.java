/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;

/**
 * This exception indicates an internal error in implementation.
 * 
 * If this exception is thrown, then the code generator cannot handle a node that is
 * created by the parser.
 */
public class UnhandledSyntaxNodeException extends CodeGeneratorImplementationException {

	/** Serial version UID. */
	private static final long serialVersionUID = -8419890206003220612L;

	/**
	 * Instantiates a new unhandled syntax node exception.
	 *
	 * @param node the unhandled node
	 */
	public UnhandledSyntaxNodeException(AbstractEqlNode node) {
		super("Cannot handle syntax node: " + node);
	}
	
}
