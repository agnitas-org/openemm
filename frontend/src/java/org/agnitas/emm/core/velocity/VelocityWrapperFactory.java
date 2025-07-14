/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;

/**
 * Factory that creates a {@link VelocityWrapper}.
 * Depending on the implementation, the factory can cache {@link VelocityWrapper} instances
 * by company IDs.
 */
public interface VelocityWrapperFactory {
	/**
	 * Returns a {@link VelocityWrapper} that runs in the context of the given company ID.
	 * 
	 * @param companyId company ID
	 * 
	 * @return instance of {@link VelocityWrapper} for given company ID
	 */
	VelocityWrapper getWrapper(int companyId);
}
