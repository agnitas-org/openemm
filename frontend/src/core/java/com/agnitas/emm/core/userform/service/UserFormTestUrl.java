/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.service;

import java.util.Objects;

public final class UserFormTestUrl {

	private final String firstname;
	private final String lastname;
	private final String email;
	private final String userFormUrl;
	
	public UserFormTestUrl(final String firstname, final String lastname, final String email, final String userFormUrl) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.userFormUrl = Objects.requireNonNull(userFormUrl);
	}

	public final String getFirstname() {
		return firstname;
	}

	public final String getLastname() {
		return lastname;
	}

	public final String getEmail() {
		return email;
	}

	public final String getUserFormUrl() {
		return userFormUrl;
	}
	
}
