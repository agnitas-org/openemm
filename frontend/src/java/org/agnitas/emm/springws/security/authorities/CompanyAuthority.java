/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.emm.springws.security.authorities;

import org.springframework.security.core.GrantedAuthority;

/**
 * Implementation of {@link GrantedAuthority} to indicate, that
 * a user has access to data of a specific company ID.
 */
public final class CompanyAuthority implements GrantedAuthority {

	/** Serial version UID. */
	private static final long serialVersionUID = 1828565463868707050L;
	
	/** The company ID. */
	private final int companyID;
	
	/**
	 * Creates a new instance for given company ID.
	 * 
	 * @param companyID company ID
	 */
	public CompanyAuthority(final int companyID) {
		this.companyID = companyID;
	}
	
	/**
	 * Returns the company ID of this authority.
	 * 
	 * @return company ID
	 */
	public final int getCompanyID() {
		return this.companyID;
	}
	
	@Override
	public final String getAuthority() {
		return String.format("COMPANY_%d", this.companyID);
	}

	@Override
	public final int hashCode() {
		return this.companyID;
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if(obj instanceof CompanyAuthority) {
			final CompanyAuthority authority = (CompanyAuthority) obj;
			
			return authority.companyID == this.companyID;
		} else {
			return false;
		}
	}

}
