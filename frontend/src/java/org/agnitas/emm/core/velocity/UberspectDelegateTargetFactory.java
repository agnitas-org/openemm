/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;

import org.apache.velocity.util.introspection.Uberspect;

/**
 * Factory for Uberspect delegate targets. These targets perform different
 * runtime checks on Velocity scripts.
 */
public interface UberspectDelegateTargetFactory {
	
	/**
	 * Create a new delegate for given company ID.
	 * 
	 * @param companyId company ID that executes the Velocity script
	 * 
	 * @return new Uberspect delegate target
	 */
	public Uberspect newDelegateTarget( int companyId);
}
