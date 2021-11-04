/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.cookies;

import java.util.Optional;

public enum SameSiteCookiePolicy {

	NONE("None"),
	LAX("Lax"),
	STRICT("Strict");
	
	private final String value;
	
	SameSiteCookiePolicy(final String value) {
		this.value = value;
	}
	
	public final String getValue() {
		return this.value;
	}
	
	public final Optional<SameSiteCookiePolicy> from(final String s) {
		if(s == null) {
			return Optional.empty();
		}
		
		for(final SameSiteCookiePolicy policy : values()) {
			if(s.equalsIgnoreCase(policy.name())) {
				return Optional.of(policy);
			}
		}
		
		return Optional.empty();
	}
	
}
