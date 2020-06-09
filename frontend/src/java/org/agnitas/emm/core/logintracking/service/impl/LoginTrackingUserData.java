/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.service.impl;

import java.util.Objects;

/**
 * Class used by LoginTrackServiceImpl via LoginTrackingUserDataAdapter only.
 */
public final class LoginTrackingUserData {

	private final String username;
	private final int companyID;
	
	public LoginTrackingUserData(final String username, final int companyID) {
		this.username = Objects.requireNonNull(username, "Username is null");
		this.companyID = companyID;
	}

	public final String getUsername() {
		return username;
	}

	public final int getCompanyID() {
		return companyID;
	}
	
}
