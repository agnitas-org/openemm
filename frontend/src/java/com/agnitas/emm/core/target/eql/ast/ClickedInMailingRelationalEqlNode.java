/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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

/**
 * Relational operator that checks, if a recipient clicked in a mailing or clicked a specific link in a mailing.
 */
@SpecialNotOperatorHandling(mustHaveReceivedMailing = true)
public final class ClickedInMailingRelationalEqlNode extends AbstractRelationalEqlNode implements DeviceQuerySupportingNode {

	/** Start of operator in EQL . */
	private final CodeLocation startLocation;
	
	/** Mailing ID. */
	private final int mailingId;

	/** Link ID. */
	private final Integer linkId;
	
	/** Optional device query. */
	private final AbstractDeviceQueryNode deviceQueryNode;
	
	/**
	 * Creates a new node with mailing ID set.
	 * 
	 * @param mailingId mailing ID
	 */
	public ClickedInMailingRelationalEqlNode(final int mailingId, final CodeLocation startLocation) {
		this.mailingId = mailingId;
		this.linkId = null;
		this.startLocation = startLocation;
		this.deviceQueryNode = null;
	}
	
	/**
	 * Creates a new node with mailing ID set.
	 * 
	 * @param mailingId mailing ID
	 */
	public ClickedInMailingRelationalEqlNode(final int mailingId, final AbstractDeviceQueryNode deviceQueryNodeOrNull, final CodeLocation startLocation) {
		this.mailingId = mailingId;
		this.linkId = null;
		this.startLocation = startLocation;
		this.deviceQueryNode = deviceQueryNodeOrNull;
		
	}
	
	/**
	 * Creates a new node with mailing ID and link ID set.
	 * 
	 * @param mailingId mailing ID
	 * @param linkId link ID
	 */
	public ClickedInMailingRelationalEqlNode(final int mailingId, final int linkId, final CodeLocation startLocation) {
		this.mailingId = mailingId;
		this.linkId = linkId;
		this.startLocation = startLocation;
		this.deviceQueryNode = null;
	}
	
	/**
	 * Creates a new node with mailing ID and link ID set.
	 * 
	 * @param mailingId mailing ID
	 * @param linkId link ID
	 */
	public ClickedInMailingRelationalEqlNode(final int mailingId, final int linkId, final AbstractDeviceQueryNode deviceQueryNodeOrNull, final CodeLocation startLocation) {
		this.mailingId = mailingId;
		this.linkId = linkId;
		this.startLocation = startLocation;
		this.deviceQueryNode = deviceQueryNodeOrNull;
	}

	/**
	 * Returns the mailing ID.
	 * 
	 * @return mailing ID
	 */
	public final int getMailingId() {
		return this.mailingId;
	}
	
	/**
	 * Returns the referenced link ID.
	 * 
	 * @return referenced link ID or null
	 */
	public final Integer getLinkId() {
		return this.linkId;
	}
	
	@Override
	public final boolean hasDeviceQuery() {
		return this.deviceQueryNode != null;
	}
	
	@Override
	public final AbstractDeviceQueryNode getDeviceQueryNode() {
		return this.deviceQueryNode;
	}
	
	/**
	 * Returns {@code true} if the node has a link ID set.
	 * 
	 * @return {@code true} if the node has a link ID set
	 */
	public final boolean hasLinkId() {
		return this.linkId != null;
	}
	
	@Override
	public final String toString() {
		if(hasLinkId()) {
			if(this.deviceQueryNode != null) {
				return String.format("(clicked-link-in-mailing %d %d %s)", mailingId, linkId, this.deviceQueryNode);
			} else {
				return String.format("(clicked-link-in-mailing %d %d)", mailingId, linkId);
			}
				
		} else {
			if(this.deviceQueryNode != null) {
				return String.format("(clicked-link-in-mailing %d %s)", mailingId, this.deviceQueryNode);
			} else {
				return String.format("(clicked-link-in-mailing %d)", mailingId);
			}
		}
	}
	
	@Override	
	public final void collectReferencedItems(final ReferenceCollector collector) {
		collector.addMailingReference(this.mailingId);
		
		if(this.linkId != null) {
			collector.addLinkReference(this.mailingId, this.linkId);
		}
	}
	
	@Override
	public final CodeLocation getStartLocation() {
		return this.startLocation;
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		TraversalUtil.traverse(deviceQueryNode, visitor);
		visitor.leavingNode(this);
	}
}
