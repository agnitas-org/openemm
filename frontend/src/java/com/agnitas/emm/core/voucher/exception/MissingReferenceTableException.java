/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.voucher.exception;

/**
 * Exception indicating missing table.
 */
public class MissingReferenceTableException extends VoucherException {

	/** Serial version UID. */
	private static final long serialVersionUID = 2129482780971980135L;

	/** Base name of missing table. */
	private final String tableBaseName;
	
	/** Company ID. */
	private final int companyID;
	
	/**
	 * Creates a new exception for given company ID indicating missing
	 * table with given base name.
	 * 
	 * @param companyID company ID
	 * @param tableBaseName base name of table
	 */
	public MissingReferenceTableException( int companyID, String tableBaseName) {
		super( "Missing reference table for company ID " + companyID + " (base name: " + tableBaseName + ")");
		
		this.tableBaseName = tableBaseName;
		this.companyID = companyID;
	}
	
	/**
	 * Returns base name of missing table.
	 * 
	 * @return base name of missing table
	 */
	public String getTableBaseName() {
		return this.tableBaseName;
	}
	
	/**
	 * Returns company ID.
	 * 
	 * @return company ID
	 */
	public int getCompanyId() {
		return this.companyID;
	}
}
