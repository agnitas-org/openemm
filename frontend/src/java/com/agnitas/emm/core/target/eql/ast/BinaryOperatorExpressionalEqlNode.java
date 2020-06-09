/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
 * Node in syntax tree representing an arithmetic expression with binary operator (like +).
 */
public final class BinaryOperatorExpressionalEqlNode extends AbstractExpressionalEqlNode {
	
	/**
	 * Enum of all expression infix operators.
	 */
	public enum InfixOperator {
		/** Addition. */
		ADD,
		
		/** Subtraction. */
		SUB,
		
		/** Multiplication. */
		MUL,
		
		/** Division. */
		DIV,
		
		/** Modulo. */
		MOD
	}
	
	/** Sub-tree on left side of operator. */
	private final AbstractExpressionalEqlNode left;
	
	/** Binary operator. */
	private final InfixOperator operator;	
	
	/** Sub-tree on right side of operator. */
	private final AbstractExpressionalEqlNode right;
	
	/**
	 * Create a new node.
	 * 
	 * @param left sub-tree of left side of operator
	 * @param operator binary operator
	 * @param right sub-tree on right side of operator
	 */
	public BinaryOperatorExpressionalEqlNode(final AbstractExpressionalEqlNode left, final InfixOperator operator, final AbstractExpressionalEqlNode right) {
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	/**
	 * Returns the sub-tree of the left side of the operator.
	 * 
	 * @return sub-tree on left side of operator.
	 */
	public final AbstractExpressionalEqlNode getLeft() {
		return left;
	}

	/**
	 * Returns the binary operator.
	 * 
	 * @return operator
	 */
	public final InfixOperator getOperator() {
		return operator;
	}

	/**
	 * Returns the sub-tree of the right side of the operator.
	 * 
	 * @return sub-tree on right side of operator.
	 */
	public final AbstractExpressionalEqlNode getRight() {
		return right;
	}

	@Override
	public final String toString() {
		return "(" + operator + " " + left + " " + right + ")";
	}
	
	@Override	
	public final void collectReferencedItems(final ReferenceCollector collector) {
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
