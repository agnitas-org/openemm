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
 * Node representing <i>EMPTY</i> / <i>NOT EMPTY</i> expressions.
 */
public final class EmptyRelationalEqlNode extends AbstractRelationalEqlNode {

	/** Expressional node to check for EMPTY/NOT EMPTY. */
	private final AbstractExpressionalEqlNode expressionNode;
	
	/** NOT-Flag. For EMPTY set to false, for NOT EMPTY set to true. */
	private final boolean notFlag;
	
	/**
	 * Creates a new node. The <i>notFlag</i> argument defined, if the operator is EMPTY (<i>notFlag</i> is set to false),
	 * or NOT EMPTY (<i>notFlag</i> is set to true).
	 * 
	 * @param expressionNode expressional node to check for EMPTY/NOT EMPTY
	 * @param notFlag flag, if operator is EMPTY or NOT EMPTY
	 */
	public EmptyRelationalEqlNode(final AbstractExpressionalEqlNode expressionNode, final boolean notFlag) {
		this.expressionNode = expressionNode;
		this.notFlag = notFlag;
	}
	
	/**
	 * Returns, if operator is NOT EMPTY.
	 * 
	 * @return true, if operator is NOT EMPTY
	 */
	public final boolean getNotFlag() {
		return this.notFlag;
	}
	
	/**
	 * Returns the expressional node to check for EMPTY / NOT EMPTY.
	 * 
	 * @return expressional node to check
	 */
	public final AbstractExpressionalEqlNode getChild() {
		return this.expressionNode;
	}
	
	@Override
	public final String toString() {
		if(notFlag)
			return ("(EMPTY " + expressionNode + ")");
		else
			return ("(NOT-EMPTY " + expressionNode + ")");
	}
	
	
	@Override	
	public final void collectReferencedItems(final ReferenceCollector collector) {
		expressionNode.collectReferencedItems(collector);
	}
	
	@Override
	public CodeLocation getStartLocation() {
		return codeLocationFromEqlNode(expressionNode);
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		TraversalUtil.traverse(expressionNode, visitor);
		visitor.leavingNode(this);
	}

}
