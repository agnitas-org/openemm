/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.voucher.exception;

/**
 * Exception indicating no free (unused) voucher codes.
 */
public class NoFreeVoucherCodeException extends VoucherException {

	/** Serial version UID. */
	private static final long serialVersionUID = -6074434864249053217L;
	
	/** Table containing voucher codes. */
	private final String table;
	
	/**
	 * Instantiates a new no free voucher code exception.
	 *
	 * @param table name of table with voucher codes
	 */
	public NoFreeVoucherCodeException( String table) {
		super( "No free voucher code in table " + table );
		
		this.table = table;
	}

	/**
	 * Returns the name of the table with voucher codes.
	 * 
	 * @return name of table with voucher codes
	 */
	public String getTable() {
		return this.table;
	}
}
