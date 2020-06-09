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
 * Node in syntax tree representing an arbitrary atomic node inside an expression.
 */
public final class AtomExpressionalEqlNode extends AbstractExpressionalEqlNode {

	/** Atomic child node. */
	private final AbstractAtomEqlNode child;
	
	/**
	 * Creates a new node.
	 * 
	 * @param child atomic child node
	 */
	public AtomExpressionalEqlNode(final AbstractAtomEqlNode child) {
		this.child = child;
	}
	
	/**
	 * Returns the atomic child node.
	 * 
	 * @return atomic child node
	 */
	public final AbstractAtomEqlNode getChild() {
		return this.child;
	}
	
	@Override
	public final String toString() {
		return this.child.toString();
	}
	
	@Override	
	public final void collectReferencedItems(final ReferenceCollector collector) {
		child.collectReferencedItems(collector);
	}
	
	@Override
	public final CodeLocation getStartLocation() {
		return codeLocationFromEqlNode(child);
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		TraversalUtil.traverse(child, visitor);
		visitor.leavingNode(this);
	}

}
