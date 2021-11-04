/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.quota.api;

public final class QuotaLimitExceededException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 8888635794063311509L;
	
	private final String username;
	private final int companyId;
	private final String apiServiceName;
	
	public QuotaLimitExceededException(final String username, final int companyId, final String apiServiceName) {
		super("Quota limit exceeded");
		
		this.username = username;
		this.companyId = companyId;
		this.apiServiceName = apiServiceName;
	}

	public String getUsername() {
		return username;
	}

	public int getCompanyId() {
		return companyId;
	}

	public String getApiServiceName() {
		return apiServiceName;
	}
	
	
	
}
