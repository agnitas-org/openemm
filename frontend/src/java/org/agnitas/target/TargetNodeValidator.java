/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target;

import java.util.Collection;

import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Interface for common validation of data of target nodes.
 */
public interface TargetNodeValidator {
	/**
	 * Performs validation of given target node. If node is free of errors, an empty collection (0 elements)
	 * or {@code null} is returned.
	 * If validator does not support given target node, a {@link RuntimeException} is thrown.
	 * 
	 * @param node node to validate
	 * @param companyId ID of company
	 * 
	 * @return Collection of {@link TargetError} or null
	 * 
	 * @throws RuntimeException in case of an unsupported target node
	 */
	public Collection<TargetError> validate( TargetNode node, @VelocityCheck int companyId);
}
