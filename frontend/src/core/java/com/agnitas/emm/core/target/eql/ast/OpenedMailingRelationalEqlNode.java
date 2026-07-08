/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import com.agnitas.emm.core.target.eql.ast.devicequery.AbstractDeviceQueryNode;
import com.agnitas.emm.core.target.eql.ast.transform.ShiftNotDownTransform.SpecialNotOperatorHandling;
import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;
import com.agnitas.emm.core.target.eql.ast.traversal.TraversalUtil;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

@SpecialNotOperatorHandling(mustHaveReceivedMailing = true)
public final class OpenedMailingRelationalEqlNode extends AbstractRelationalEqlNode implements DeviceQuerySupportingNode {

	private final CodeLocation startLocation;
	private final int mailingId;
	private final AbstractDeviceQueryNode deviceQueryNode;
	
	public OpenedMailingRelationalEqlNode(final int mailingId, final CodeLocation startLocation) {
		this.mailingId = mailingId;
		this.startLocation = startLocation;
		this.deviceQueryNode = null;
	}
	
	public OpenedMailingRelationalEqlNode(final int mailingId, final AbstractDeviceQueryNode deviceQueryNodeOrNull, final CodeLocation startLocation) {
		this.mailingId = mailingId;
		this.startLocation = startLocation;
		this.deviceQueryNode = deviceQueryNodeOrNull;
	}

	@Override
	public final CodeLocation getStartLocation() {
		return this.startLocation;
	}

	/**
	 * Returns the ID of the mailing to be checked for opening.
	 * 
	 * @return ID of mailing
	 */
	public final int getMailingId() {
		return this.mailingId;
	}
	
	/**
	 * Returns the node of the device query or <code>null</code>.
	 * 
	 * @return node of device query or <code>null</code>
	 */
	@Override
	public final AbstractDeviceQueryNode getDeviceQueryNode() {
		return this.deviceQueryNode;
	}
	
	@Override
	public final String toString() {
		if(this.deviceQueryNode == null) {
			return String.format("(opened-mailing %d)", this.mailingId);
		} else {
			return String.format("(opened-mailing %d %s)", this.mailingId, this.deviceQueryNode.toString());
		}
	}
	
	@Override	
	public final void collectReferencedItems(final ReferenceCollector collector) {
		collector.addMailingReference(this.mailingId);
	}

	@Override
	public final boolean hasDeviceQuery() {
		return this.deviceQueryNode != null;
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		TraversalUtil.traverse(deviceQueryNode, visitor);
		visitor.leavingNode(this);
	}
}
