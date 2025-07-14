/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.springws.security.authorities;

import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;

/**
 * Implementation of {@link GrantedAuthority} to indicate that a user
 * has access to a specific endpoint.
 * 
 * Instances of this class are mapped to webservice_permission_tbl.
 * 
 * <p>
 * <b>Important note:</b>
 * Use the basic class name of the endpoint class for endpoint name here.
 * If an endpoint class is named <pre>foo.BarEndpoint</pre>, remove package and the
 * &quot;Endpoint&quot; suffix, so the endpoint will be named &quot;Bar&quot;.
 * </p>
 */
public final class EndpointAuthority implements GrantedAuthority {
	
	/** Serial version UID. */
	private static final long serialVersionUID = 4896857656140774974L;
	
	/** Endpoint name. */
	private final String endpoint;
	
	/**
	 * Creates a new instance with given endpoint name.
	 * 
	 * @param endpoint endpoint name
	 * 
	 * @throws NullPointerException if <code>name</code> is <code>null</code>
	 */
	public EndpointAuthority(final String endpoint) {
		this.endpoint = Objects.requireNonNull(endpoint, "Name of granted endpoint is null");
	}

	@Override
	public final String getAuthority() {
		return String.format("ENDPOINT_%s", endpoint);
	}
	
	/**
	 * Returns the name of the endpoint.
	 * 
	 * @return name of the endpoint
	 */
	public final String getEndpoint() {
		return this.endpoint;
	}

	@Override
	public final boolean equals(final Object obj) {
		if(obj instanceof EndpointAuthority) {
			final EndpointAuthority authority = (EndpointAuthority) obj;
			
			return authority.endpoint.equals(this.endpoint);
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return this.endpoint.hashCode();
	}
}
