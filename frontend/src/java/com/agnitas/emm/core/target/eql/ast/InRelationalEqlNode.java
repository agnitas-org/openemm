/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;
import com.agnitas.emm.core.target.eql.ast.traversal.TraversalUtil;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

public final class InRelationalEqlNode extends AbstractRelationalEqlNode {

	private final AbstractExpressionalEqlNode expressionNode;
	private final ConstantListEqlNode listNode;
	private final boolean notFlag;
	
	public InRelationalEqlNode(final AbstractExpressionalEqlNode expressionNode, final ConstantListEqlNode listNode, final boolean notFlag) {
		this.expressionNode = expressionNode;
		this.listNode = listNode;
		this.notFlag = notFlag;
	}
	
	public final boolean getNotFlag() {
		return this.notFlag;
	}
	
	public final AbstractExpressionalEqlNode getLeft() {
		return this.expressionNode;
	}
	
	public final ConstantListEqlNode getRight() {
		return this.listNode;
	}
	
	@Override
	public final String toString() {
		if(notFlag) {
			return ("(NOT-IN " + expressionNode + " " + listNode + ")");
		} else {
			return ("(IN " + expressionNode + " " + listNode + ")");
		}
	}
	
	
	@Override	
	public final void collectReferencedItems(final ReferenceCollector collector) {
		expressionNode.collectReferencedItems(collector);

		// TODO: Verify: Lists cannot contain mailing or link id or profile fields, ...
	}
	
	@Override
	public CodeLocation getStartLocation() {
		return codeLocationFromEqlNode(expressionNode);
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		TraversalUtil.traverse(expressionNode, visitor);
		TraversalUtil.traverse(listNode, visitor);
		visitor.leavingNode(this);
	}

}
