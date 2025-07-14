/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.service;

public final class FailedToAssignCompanyTokenException extends CompanyTokenException {
	private static final long serialVersionUID = -2287513873251091764L;
	
	private final int companyID;
	
	public FailedToAssignCompanyTokenException(final int companyID, final String message) {
		super(String.format("Failed to assign token to company %d: %s", companyID, message));

		this.companyID = companyID;
	}
	
	public final int getComanyID() {
		return this.companyID;
	}
}
