/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.emm.springws.security.authorities;

import org.springframework.security.core.GrantedAuthority;

/**
 * Implementation of {@link GrantedAuthority} to indicate that the user has
 * access to all endpoints.
 * 
 * <p>
 * <b>Important note:</b>
 * Rather than {@link EndpointAuthority}, this class is not mapped to any value 
 * in <i>webservice_permission_tbl</i>! This is used only in the case that
 * web service permissions are disabled for a company.
 * </p>
 */
public final class AllEndpointsAuthority implements GrantedAuthority {
	
	/** Serial version UID. */
	private static final long serialVersionUID = -600399284900086048L;
		
	/** Instance of this class. */
	public static final AllEndpointsAuthority INSTANCE = new AllEndpointsAuthority();

	@Override
	public final String getAuthority() {
		return "ENDPOINTS_ALL";
	}
	
	@Override
	public final boolean equals(final Object obj) {
		return obj instanceof AllEndpointsAuthority;
	}

	@Override
	public final int hashCode() {
		return 0; // Since this class has no state, we can return same hash code for all instances
	}

}
