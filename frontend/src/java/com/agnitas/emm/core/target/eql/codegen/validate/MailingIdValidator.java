/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.validate;

/**
 * Common interface for validating mailing IDs.
 *  Validation can include these checks:
 * <ul>
 *   <li>Value is a positive, integer value</li>
 *   <li>Mailing ID is associated to given company ID</li>
 *   <li>may be: Mailing is not deleted</li>
 * </ul>
 */
public interface MailingIdValidator {

	/**
	 * Validates given mailing ID.
	 * 
	 * @param mailingId mailing ID to validate
	 * @param companyId company ID the mailing ID must be associated with.
	 * 
	 * @throws MailingIdValidationException on errors during validation of mailing ID
	 */
	public void validateMailingId(int mailingId, int companyId) throws MailingIdValidationException;
}
