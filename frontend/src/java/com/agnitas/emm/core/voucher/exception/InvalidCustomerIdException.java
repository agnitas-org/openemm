/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.voucher.exception;

/**
 * Exception indicating invalid customer ID.
 */
public class InvalidCustomerIdException extends VoucherException {

	/** Serial version UID. */
	private static final long serialVersionUID = -1416887590546057328L;

	/** Invalid customer ID. */
	private final int customerID;
	
	/**
	 * Create exception with invalid customer ID.
	 * 
	 * @param customerID invalid customer ID
	 * @param message 
	 */
	public InvalidCustomerIdException( int customerID, String message) {
		super( "Invalid customer ID: " + customerID + " - " + message);
		
		this.customerID = customerID;
	}
	
	/**
	 * Returns invalid customer ID. 
	 * 
	 * @return invalid customer ID
	 */
	public int getCustomerId() {
		return this.customerID;
	}
}
