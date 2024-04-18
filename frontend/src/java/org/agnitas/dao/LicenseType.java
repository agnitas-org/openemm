/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

public enum LicenseType {
	Saas("Saas"),
	Inhouse("Inhouse"),
	OpenEMM("OpenEMM"),
	OpenEMM_Plus("OpenEMM_Plus");
	
	private String id;
	
	LicenseType(final String id) {
		this.id = id;
	}
	
	public String getCode() {
		return id;
	}

	public static LicenseType getLicenseTypeByID(String id) {
		for (LicenseType licenseType : LicenseType.values()) {
			if (id != null && licenseType.id.replace("_", "").equalsIgnoreCase(id.replace("_", ""))) {
				return licenseType;
			}
		}
		throw new RuntimeException("Unknown license type id: " + id);
	}
}
