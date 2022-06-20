/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import java.util.Optional;

import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;
import com.agnitas.emm.core.target.eql.ast.traversal.TraversalUtil;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

/**
 * EQL node representing empty target rule.
 */
public final class BooleanExpressionTargetRuleEqlNode extends AbstractEqlNode {
	
	private final CodeLocation codeLocation;
	private final Optional<AbstractBooleanEqlNode> child;
	
	/**
	 * Creates a new EQL node with given {@link CodeLocation}.
	 * 
	 * @param childNode subsequent Boolean node
	 */
	public BooleanExpressionTargetRuleEqlNode(final AbstractBooleanEqlNode childNode) {
		this.codeLocation = childNode.getStartLocation();
		this.child = Optional.of(childNode);
	}
	
	/**
	 * Creates a new EQL node with given {@link CodeLocation}.
	 * 
	 * @param location {@link CodeLocation}
	 */
	public BooleanExpressionTargetRuleEqlNode(final CodeLocation location) {
		this.codeLocation = location;
		this.child = Optional.empty();
	}
	
	/**
	 * Returns the optional child node. If node is not present, no target rule has been defined.
	 * 
	 * @return optional AbstractBooleanEqlNode
	 */
	public final Optional<AbstractBooleanEqlNode> getChild() {
		return this.child;
	}

	@Override
	public final String toString() {
		return this.child.isPresent() ? this.child.get().toString() : "(empty)";
	}

	@Override
	public final void collectReferencedItems(final ReferenceCollector collector) {
		if(child.isPresent()) {
			child.get().collectReferencedItems(collector);
		}
	}

	@Override
	public final CodeLocation getStartLocation() {
		return this.codeLocation;
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		if(child.isPresent()) {
			TraversalUtil.traverse(child.get(), visitor);
		}
		visitor.leavingNode(this);
	}

}
