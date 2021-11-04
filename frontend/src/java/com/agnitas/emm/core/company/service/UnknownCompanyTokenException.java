/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.service;

public final class UnknownCompanyTokenException extends CompanyTokenException {
	private static final long serialVersionUID = -4579922702274876263L;
	
	private final String token;
	
	public UnknownCompanyTokenException(final String token) {
		super(String.format("Unknown company token: '%s'", token));
		
		this.token = token;
	}
	
	public UnknownCompanyTokenException(final String token, final Throwable cause) {
		super(String.format("Unknown company token: '%s'", token), cause);
		
		this.token = token;
	}
	
	public final String getToken() {
		return this.token;
	}
	
}
