/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast.analysis;

import java.util.Stack;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.ast.DeviceQuerySupportingNode;
import com.agnitas.emm.core.target.eql.ast.NotOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;

public abstract class AbstractNotOperatorSyntaxTreeAnalyzer implements EqlNodeVisitor {
	
	private final Stack<Boolean> notStateStack;

	public AbstractNotOperatorSyntaxTreeAnalyzer() {
		this.notStateStack = new Stack<>();
		notStateStack.push(Boolean.FALSE);
	}
	
	@Override
	public void enteredNode(AbstractEqlNode node) {
		nodeVisited(node, notStateStack.peek());
		
		if(node instanceof NotOperatorBooleanEqlNode) {
			notStateStack.push(!notStateStack.peek());
		} else if (node instanceof DeviceQuerySupportingNode) {
			// For all nodes supporting Device Queries, the device query starts un-negated
			notStateStack.push(Boolean.FALSE);
		} else {
			// Duplicate top of stack
			notStateStack.push(notStateStack.peek());
		}
	}

	@Override
	public void leavingNode(AbstractEqlNode node) {
		notStateStack.pop();
	}
	
	public abstract void nodeVisited(final AbstractEqlNode node, final boolean not);

}
