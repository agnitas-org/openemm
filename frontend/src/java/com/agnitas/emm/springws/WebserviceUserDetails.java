/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.springws;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public final class WebserviceUserDetails extends User {
	private static final long serialVersionUID = 3116668762175491151L;
	
	private final int companyID;
	
	public WebserviceUserDetails(final String username, final int companyID, final String password, final boolean enabled, final boolean accountNonExpired,
			final boolean credentialsNonExpired, final boolean accountNonLocked, final Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);

		this.companyID = companyID;
	}

	public final int getCompanyID() {
		return this.companyID;
	}
	
}
