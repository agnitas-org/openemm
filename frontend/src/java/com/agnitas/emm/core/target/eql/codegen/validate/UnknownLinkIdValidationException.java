/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.validate;

/**
 * Exception indicating an unknown link ID during validation.
 */
public class UnknownLinkIdValidationException extends LinkIdValidationException {

	/** Serial version UID. */
	private static final long serialVersionUID = 3750213025314974998L;
	
	/** Unknown link ID. */
	private final int linkId;
	
	/** Company ID for link. */
	private final int companyId;
	
	/** Mailing ID for link. */
	private final int mailingId;
	
	/**
	 * Creates a new exception for given IDs.
	 * 
	 * @param linkId unknown link ID
	 * @param mailingId used mailing ID
	 * @param companyId used company ID
	 */
	public UnknownLinkIdValidationException(final int linkId, final int mailingId, final int companyId) {
		super("Unknown link ID " + linkId + " for mailing " + mailingId);
		
		this.linkId = linkId;
		this.mailingId = mailingId;
		this.companyId = companyId;
	}

	/**
	 * Returns unknown link ID.
	 * 
	 * @return unknown link ID
	 */
	public int getLinkId() {
		return linkId;
	}

	/**
	 * Returns company ID.
	 * 
	 * @return company ID
	 */
	public int getCompanyId() {
		return companyId;
	}

	/**
	 * Returns mailing ID.
	 * 
	 * @return mailing ID
	 */
	public int getMailingId() {
		return mailingId;
	}
		
}
