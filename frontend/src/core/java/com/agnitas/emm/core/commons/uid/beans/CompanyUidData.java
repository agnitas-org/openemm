/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.beans;

import com.agnitas.beans.Company;

public final class CompanyUidData {

	private final String secretKey;
	private final int enabledUIDVersion;
	private final Number minimumSupportedUIDVersion;
	
	public CompanyUidData(final String secretKey, final int enabledUIDVersion, final Number minimumSupportedUIDVersion) {
		this.secretKey = secretKey;
		this.enabledUIDVersion = enabledUIDVersion;
		this.minimumSupportedUIDVersion = minimumSupportedUIDVersion;
	}
	
	public static CompanyUidData from(final Company company) {
		return new CompanyUidData(company.getSecretKey(), company.getEnabledUIDVersion(), company.getMinimumSupportedUIDVersion());
	}

	public final String getSecretKey() {
		return secretKey;
	}

	public final int getEnabledUIDVersion() {
		return enabledUIDVersion;
	}
	
	public final Number getMinimumSupportedUIDVersion() {
		return this.minimumSupportedUIDVersion;
	}
}
