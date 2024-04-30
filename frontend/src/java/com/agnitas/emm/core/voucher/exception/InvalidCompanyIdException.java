/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.voucher.exception;

/**
 * Exception indicating an invalid company ID.
 */
public class InvalidCompanyIdException extends VoucherException {

	/** Serial version UID. */
	private static final long serialVersionUID = -1416887590546057328L;

	/** Invalid company ID. */
	private final int companyID;
	
	/**
	 * Create new exception with given invalid company ID.
	 * 
	 * @param message additinal information
	 * @param companyID invalid company ID
	 */
	public InvalidCompanyIdException(int companyID, String message) {
		super( "Invalid company ID: " + companyID + " - " + message);
		
		this.companyID = companyID;
	}
	
	/**
	 * Returns invalid company ID.
	 * 
	 * @return invalid company ID
	 */
	public int getCompanyId() {
		return this.companyID;
	}
}
