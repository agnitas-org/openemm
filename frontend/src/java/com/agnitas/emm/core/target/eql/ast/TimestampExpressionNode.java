/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import java.util.Objects;

import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

/**
 * Node representing a timestamp expression.
 * 
 * Allows comparison of a timestamp (like a binding timestamp) with a given date.
 */
public final class TimestampExpressionNode extends AbstractEqlNode {

	/** Operator for comparison. */
	private final RelationalInfixOperator operator;
	
	/** Expression to compare timestamp with. */
	private final AbstractExpressionalEqlNode expressionalNode;
	
	/** Date format for comparison. */
	private final String dateformat;
	
	/** Start of code in source code. */
	private final CodeLocation startLocation;
	
	public TimestampExpressionNode(final RelationalInfixOperator operator, final AbstractExpressionalEqlNode expressionalNode, final String dateFormat, final CodeLocation location) {
		this.operator = Objects.requireNonNull(operator, "Operator is null");
		this.expressionalNode = Objects.requireNonNull(expressionalNode, "Expressional node is null");
		this.dateformat = Objects.requireNonNull(dateFormat, "Date format is null");
		this.startLocation = Objects.requireNonNull(location, "Code location is null");
	}
	
	/**
	 * Returns the operator for comparison.
	 *  
	 * @return operator
	 */
	public final RelationalInfixOperator getOperator() {
		return operator;
	}

	/**
	 * Returns the expression to compare timestamp with.
	 * 
	 * @return expression
	 */
	public final AbstractExpressionalEqlNode getExpressionalNode() {
		return expressionalNode;
	}

	/**
	 * Returns the date format used for comparison.
	 * 
	 * @return date format
	 */
	public final String getDateFormat() {
		return dateformat;
	}

	@Override
	public final CodeLocation getStartLocation() {
		return this.startLocation;
	}

	@Override
	public final String toString() {
		return String.format("(timestamp-expression %s %s (*date-format* %s *))" , operator, expressionalNode, dateformat);
	}

	@Override
	public final void collectReferencedItems(final ReferenceCollector collector) {
		expressionalNode.collectReferencedItems(collector);
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		this.expressionalNode.traverse(visitor);
		visitor.leavingNode(this);
	}

}
