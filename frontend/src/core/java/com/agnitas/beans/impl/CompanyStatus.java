/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

public enum CompanyStatus {

	ACTIVE("active", true),
	LOCKED("locked", false),
	TODELETE("todelete", false),
	DELETION_IN_PROGRESS("deletion in progress", false),
	DELETED("deleted", false),
	TORESET("toreset", false);

	public final String dbValue;
	public final boolean companyEditable;
	
	CompanyStatus(final String dbValue, final boolean editable) {
		this.dbValue = dbValue;
		this.companyEditable = editable;
	}
	
	public String getDbValue() {
		return dbValue;
	}
	
	public static CompanyStatus getCompanyStatus(String dbValue) {
		if (dbValue == null) {
			throw new IllegalArgumentException("Invalid CompanyStatus dbValue: null");
		}

		for (CompanyStatus status : values()) {
			if (status.getDbValue().replace(" ", "").replace("_", "").replace("-", "").equalsIgnoreCase(dbValue.replace(" ", "").replace("_", "").replace("-", ""))) {
				return status;
			}
		}
		throw new IllegalArgumentException("Invalid CompanyStatus dbValue: " + dbValue);
	}
}
