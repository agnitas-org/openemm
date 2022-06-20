/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;
import com.agnitas.emm.core.target.eql.ast.traversal.TraversalUtil;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

/**
 * Node in syntax tree representing an boolean expression with binary operator (like AND).
 */
public final class BinaryOperatorBooleanEqlNode extends AbstractBooleanEqlNode {

	/**
	 * Enum of all boolean infix operators.
	 */
	public enum InfixOperator {
		/** AND operator. */
		AND,
		
		/** OR operator. */
		OR
	}
	
	/** Sub-tree on left side of operator. */
	private final AbstractBooleanEqlNode left;
	
	/** Sub-tree on right side of operator. */
	private final AbstractBooleanEqlNode right;
	
	/** Binary operator. */
	private final InfixOperator operator;

	/**
	 * Create a new node.
	 * 
	 * @param left sub-tree of left side of operator
	 * @param operator binary operator
	 * @param right sub-tree on right side of operator
	 */
	public BinaryOperatorBooleanEqlNode(final AbstractBooleanEqlNode left, final InfixOperator operator, final AbstractBooleanEqlNode right) {
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	/**
	 * Returns the sub-tree of the left side of the operator.
	 * 
	 * @return sub-tree on left side of operator.
	 */
	public final AbstractBooleanEqlNode getLeft() {
		return left;
	}
	
	/**
	 * Returns the sub-tree of the right side of the operator.
	 * 
	 * @return sub-tree on right side of operator.
	 */
	public final AbstractBooleanEqlNode getRight() {
		return right;
	}

	/**
	 * Returns the binary operator.
	 * 
	 * @return operator
	 */
	public final InfixOperator getOperator() {
		return operator;
	}


	@Override
	public final String toString() {
		return "(" + operator + " " + left + " " + right + ")";
	}
	
	@Override
	public final void collectReferencedItems(ReferenceCollector collector) {
		left.collectReferencedItems(collector);
		right.collectReferencedItems(collector);
	}
	
	@Override
	public final CodeLocation getStartLocation() {
		return codeLocationFromEqlNode(left);
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		TraversalUtil.traverse(left, visitor);
		TraversalUtil.traverse(right, visitor);
		visitor.leavingNode(this);
	}

}
