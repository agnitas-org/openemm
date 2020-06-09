/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;

@Deprecated
public interface TargetRepresentation extends Serializable {
	/**
	 * Adds one node.
	 */
	public void addNode(TargetNode aNode);

	/**
	 * Checks if all opened Brackes are closed.
	 */
	public boolean checkBracketBalance();

	/**
	 * Removes one node.
	 */
	public boolean deleteNode(int index);

	/**
	 * Generates bsh.
	 */
	// TODO Move method to EqlFacade
	public String generateBsh();

	/**
	 * Getter for allNodes.
	 * 
	 * @return Value of allNodes.
	 */
	public List<TargetNode> getAllNodes();

	/**
	 * Setter for property node.
	 * 
	 * @param aNode
	 *            New value of property node.
	 */
	public void setNode(int idx, TargetNode aNode);
	
	/**
	 * Validate all rules of the target group. Each element of the returned list contains all errors found for the corresponding rule.
	 * Rules with no errors are shown by {@code null} or empty collections.
	 * 
	 * @param validatorKit validator kit used for validation
	 * @param companyId ID of company
	 * 
	 * @return List of error data
	 */
	public List<Collection<TargetError>> validate( TargetNodeValidatorKit validatorKit, @VelocityCheck int companyId);
	
	/**
	 * Checks, if this instance of {@link TargetRepresentation} has same node structure as given instance.
	 * 
	 * @param representation instance of {@link TargetRepresentation} to compare with
	 * 
	 * @return true if node structures are identical
	 */
	public boolean hasSameNodeStructureAs(TargetRepresentation representation);
}
