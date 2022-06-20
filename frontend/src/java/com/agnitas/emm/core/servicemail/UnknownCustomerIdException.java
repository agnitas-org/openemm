/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.servicemail;

/**
 * Exception indicating an unknown customer ID used for some
 * operations on service mails.
 */
public class UnknownCustomerIdException extends ServiceMailException {

	/** Serial version UID. */
	private static final long serialVersionUID = 7398895816145723423L;
	
	/** Customer ID. */
	private final int customerID;
	
	/**
	 * Creates a new exception with given unknown customer ID.
	 * 
	 * @param customerID unknown customer ID
	 */
	public UnknownCustomerIdException(final int customerID) {
		super("Unknown customer ID: " + customerID);
		
		this.customerID = customerID;
	}
	
	/**
	 * Returns unknown customer ID.
	 * 
	 * @return unknown customer ID
	 */
	public int getCustomerID() {
		return this.customerID;
	}
}
