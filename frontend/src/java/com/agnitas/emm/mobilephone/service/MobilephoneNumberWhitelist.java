/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.mobilephone.service;

import com.agnitas.emm.mobilephone.MobilephoneNumber;

/**
 * Interface for checking mobile phone numbers against a whitelist.
 */
public interface MobilephoneNumberWhitelist {

	/**
	 * Checks if given mobile phone number is whitelisted.
	 * 
	 * @param number mobile phone number
	 * @param companyID ID of whitelist owner company
	 * 
	 * @return <code>true</code> if number is whitelisted
	 */
	public boolean isWhitelisted(final MobilephoneNumber number, final int companyID);
	
}
