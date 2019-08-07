/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.validate;

/**
 * Common interface for validating a link ID. 
 * Validation can include these checks:
 * <ul>
 *   <li>Value is a positive, integer value</li>
 *   <li>Link ID is associated to given mailing ID</li>
 *   <li>Link ID is associated to given company ID</li>
 *   <li>may be: Link is not deleted</li>
 * </ul>
 */
public interface LinkIdValidator {

	/**
	 * Validate given link ID.
	 * 
	 * @param mailingId mailing ID to check, if link is associated to it.
	 * @param linkId link ID to check (this value can be null if no link was specified)
	 * @param companyId company ID the link (and mailing) must be associated to
	 * 
	 * @throws LinkIdValidationException on errors validating the link ID
	 */
	void validateLinkId(int mailingId, Integer linkId, int companyId) throws LinkIdValidationException;

}
