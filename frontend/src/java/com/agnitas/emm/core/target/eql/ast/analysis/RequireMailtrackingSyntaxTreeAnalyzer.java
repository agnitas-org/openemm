/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast.analysis;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.ast.ClickedInMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.OpenedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ReceivedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.RevenueByMailingRelationalEqlNode;

public final class RequireMailtrackingSyntaxTreeAnalyzer extends AbstractNotOperatorSyntaxTreeAnalyzer {

	private boolean mailtrackingRequired;
	
	public final boolean isMailtrackingRequired() {
		return this.mailtrackingRequired;
	}
	
	@Override
	public final void nodeVisited(final AbstractEqlNode node, final boolean not) {
		mailtrackingRequired |=	isReceivedMailingNode(node) ||
				(not && isRelevantNodeType(node));
	}

	private static boolean isReceivedMailingNode(final AbstractEqlNode node) {
		return node instanceof ReceivedMailingRelationalEqlNode;
	}
	
	private static boolean isRelevantNodeType(final AbstractEqlNode node) {
		return 
				node instanceof OpenedMailingRelationalEqlNode
				|| node instanceof ClickedInMailingRelationalEqlNode
				|| node instanceof RevenueByMailingRelationalEqlNode;
	}

}
