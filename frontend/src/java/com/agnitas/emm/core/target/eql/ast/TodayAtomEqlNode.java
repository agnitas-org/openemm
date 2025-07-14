/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

/**
 * Syntax node representing atomic <i>TODAY</i> constant.
 */
public class TodayAtomEqlNode extends AbstractAtomEqlNode {

	private final CodeLocation startLocation;
	
	public TodayAtomEqlNode(CodeLocation startLocation) {
		this.startLocation = startLocation;
	}
	
	@Override
	public CodeLocation getStartLocation() {
		return this.startLocation;
	}
	
	@Override
	public String toString() {
		return "(*today*)";
	}
	
	@Override	
	public void collectReferencedItems(ReferenceCollector collector) {
		// TODAY is never an ID or profile field -> nothing to collect
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		visitor.leavingNode(this);
	}

}
