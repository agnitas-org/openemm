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

public class NumericConstantListItemEqlNode extends AbstractConstantListItemEqlNode {

	private final NumericConstantAtomEqlNode node;
	
	public NumericConstantListItemEqlNode(NumericConstantAtomEqlNode node) {
		this.node = node;
	}
	
	public NumericConstantAtomEqlNode getNumericConstantNode() {
		return this.node;
	}
	
	@Override
	public String toString() {
		return node.toString();
	}
	
	@Override
	public CodeLocation getStartLocation() {
		return codeLocationFromEqlNode(node);
	}

	@Override
	public void collectReferencedItems(ReferenceCollector collector) {
		// Nothing to do here
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		TraversalUtil.traverse(node, visitor);
		visitor.leavingNode(this);
	}
}
