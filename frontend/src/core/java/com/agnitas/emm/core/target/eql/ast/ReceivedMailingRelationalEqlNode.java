/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import com.agnitas.emm.core.target.eql.ast.transform.ShiftNotDownTransform.SpecialNotOperatorHandling;
import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

@SpecialNotOperatorHandling
public final class ReceivedMailingRelationalEqlNode extends AbstractRelationalEqlNode {

	private final CodeLocation startLocation;
	private final int mailingId;
	
	public ReceivedMailingRelationalEqlNode(final int mailingId, final CodeLocation startLocation) {
		this.mailingId = mailingId;
		this.startLocation = startLocation;
	}
	
	@Override
	public final CodeLocation getStartLocation() {
		return this.startLocation;
	}
	
	/**
	 * Returns mailing ID.
	 * 
	 * @return mailing ID
	 */
	public final int getMailingId() {
		return this.mailingId;
	}
	
	@Override
	public final String toString() {
		return "(received-mailing " + mailingId + ")";
	}
	
	@Override	
	public final void collectReferencedItems(final ReferenceCollector collector) {
		collector.addMailingReference(mailingId);
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		visitor.leavingNode(this);
	}

}
