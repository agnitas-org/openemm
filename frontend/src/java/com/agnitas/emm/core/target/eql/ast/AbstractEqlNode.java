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

public abstract class AbstractEqlNode {

	public abstract CodeLocation getStartLocation();

	public static CodeLocation codeLocationFromEqlNode(AbstractEqlNode node) {
		if(node != null) {
			return node.getStartLocation();
		} else {
			return null;
		}
	}

	@Override
	public abstract String toString();	/* Force all sub-classes to implement toString() */

	/**
	 * Collects all items referenced in given node.
	 * 
	 * @param collector instance of {@link ReferenceCollector}
	 */
	public abstract void collectReferencedItems(ReferenceCollector collector);
	
	public abstract void traverse(final EqlNodeVisitor visitor);
}
