/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

public final class LikeRelationalEqlNode extends AbstractRelationalEqlNode {

	private final AbstractExpressionalEqlNode expressionNode;
	private final StringConstantWithEscapeCharsAtomEqlNode stringConstantNode;
	private final boolean notFlag;
	
	public LikeRelationalEqlNode(final AbstractExpressionalEqlNode expressionNode, final StringConstantWithEscapeCharsAtomEqlNode stringConstantNode, final boolean notFlag) {
		this.expressionNode = expressionNode;
		this.stringConstantNode = stringConstantNode;
		this.notFlag = notFlag;
	}
	
	public final AbstractExpressionalEqlNode getLeft() {
		return this.expressionNode;
	}
	
	public final boolean getNotFlag() {
		return this.notFlag;
	}
	
	public final StringConstantWithEscapeCharsAtomEqlNode getRight() {
		return this.stringConstantNode;
	}
	
	@Override
	public final String toString() {
		if(notFlag) {
			return ("(NOT-LIKE " + expressionNode + " " + stringConstantNode + ")");
		} else {
			return ("(LIKE " + expressionNode + " " + stringConstantNode + ")");
		}
	}
	
	@Override	
	public final void collectReferencedItems(final ReferenceCollector collector) {
		expressionNode.collectReferencedItems(collector);
	}
	
	@Override
	public final CodeLocation getStartLocation() {
		return codeLocationFromEqlNode(expressionNode);
	}
}
