/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailloop;

/**
 * Exception indicating an unknown mailloop ID in given company ID.
 */
@Deprecated
public class UnknownMailloopIdException extends MailloopException {

	/** Serial version UID. */
	private static final long serialVersionUID = 495147329291199596L;
	
	/** Unknown mailloop ID. */
	private final int mailloopID;
	
	/** Company ID. */
	private final int companyID;
	
	/**
	 * Creates a new exception indicating an unknown mailloop ID for given company ID.
	 * 
	 * @param mailloopID unknown mailloop ID
	 * @param companyID company ID
	 */
	public UnknownMailloopIdException(final int mailloopID, final int companyID) {
		super("Unknown mailloop ID " + mailloopID + " (company " + companyID + ")");
		
		this.mailloopID = mailloopID;
		this.companyID = companyID;
	}

	/**
	 * Returns unknown mailloop ID.
	 * 
	 * @return unknown mailloop ID
	 */
	public int getMailloopID() {
		return mailloopID;
	}

	/**
	 * Returns company ID
	 * @return company ID
	 */
	public int getCompanyID() {
		return companyID;
	}
	
	
}
