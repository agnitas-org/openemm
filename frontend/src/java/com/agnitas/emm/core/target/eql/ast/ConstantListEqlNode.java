/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;
import com.agnitas.emm.core.target.eql.ast.traversal.TraversalUtil;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

public class ConstantListEqlNode extends AbstractEqlNode {
	
	private final List<AbstractConstantListItemEqlNode> elements;
	private final List<AbstractConstantListItemEqlNode> unmodifyableElements;
	
	public ConstantListEqlNode() {
		this.elements = new Vector<>();
		this.unmodifyableElements = Collections.unmodifiableList(this.elements);
	}

	public void addListElement(AbstractConstantListItemEqlNode itemNode) {
		this.elements.add(itemNode);
	}
	
	public List<AbstractConstantListItemEqlNode> elements() {
		return this.unmodifyableElements;
	}
	
	public int listSize() {
		return this.elements.size();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("(*list* ");
		for(int i = 0; i < elements.size(); i++) {
			if(i > 0)
				buffer.append(" ");
			buffer.append("(*element* ");
			buffer.append(elements.get(i));
			buffer.append(")");
		}
		buffer.append(")");
		
		return buffer.toString();
	}
	
	@Override
	public CodeLocation getStartLocation() {
		if(elements != null && elements.size() > 0) {
			return codeLocationFromEqlNode(elements.get(0));
		} else {
			return null;
		}
	}

	@Override
	public void collectReferencedItems(ReferenceCollector collector) {
		// Nothing to do here
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		for(final AbstractEqlNode node : this.elements) {
			TraversalUtil.traverse(node, visitor);
		}
		visitor.leavingNode(this);
	}
}
