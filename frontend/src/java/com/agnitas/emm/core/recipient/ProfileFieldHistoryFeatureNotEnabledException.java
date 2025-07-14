/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient;

public class ProfileFieldHistoryFeatureNotEnabledException extends RecipientProfileHistoryException {
	private static final long serialVersionUID = -6463153230196458312L;
	
	private final int companyID;
	
	public ProfileFieldHistoryFeatureNotEnabledException(final int companyID) {
		super(String.format("Profile field history not enabled for company %d", companyID));
		
		this.companyID = companyID;
	}
	
	public int getCompanyID() {
		return this.companyID;
	}
}
