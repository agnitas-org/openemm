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

public final class RelationalInfixOperatorNode extends AbstractEqlNode {

	private final CodeLocation startLocation;
	private final RelationalInfixOperator operator;
	
	public RelationalInfixOperatorNode(final RelationalInfixOperator operator, final CodeLocation location) {
		this.operator = Objects.requireNonNull(operator, "Operator is null");
		this.startLocation = Objects.requireNonNull(location, "Code location is null");
	}
	
	public final RelationalInfixOperator getOperator() {
		return this.operator;
	}
	
	@Override
	public final CodeLocation getStartLocation() {
		return this.startLocation;
	}

	@Override
	public final String toString() {
		return operator.toString();
	}

	@Override
	public final void collectReferencedItems(final ReferenceCollector collector) {
		// Does nothing
	}

	@Override
	public final void traverse(EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		visitor.leavingNode(this);
	}

}
