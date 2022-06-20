/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.servicemail;

/**
 * Exception indicating an unknown company ID used for some
 * operations on service mails.
 */
public class UnknownCompanyIdException extends ServiceMailException {
	
	/** Serial version UID. */
	private static final long serialVersionUID = -412373188206447071L;
	
	/** Company ID. */
	private final int companyID;
	
	/**
	 * Creates a new exception with given unknown company ID.
	 * 
	 * @param companyID unknown company ID
	 */
	public UnknownCompanyIdException(final int companyID) {
		super("Unknown company ID: " + companyID);
		
		this.companyID = companyID;
	}

	/**
	 * Returns unknown company ID.
	 * 
	 * @return unknown company ID
	 */
	public int getCompanyID() {
		return this.companyID;
	}

	
}
