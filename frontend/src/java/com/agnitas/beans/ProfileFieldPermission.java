/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

public class ProfileFieldPermission {
	private int companyId = 0;
	private String columnName = null;
	private int adminId = 0;
	private ProfileFieldMode modeEdit = ProfileFieldMode.Editable;
	
	public int getCompanyId() {
		return companyId;
	}
	
	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String columnName) {
		if (columnName == null) {
			this.columnName = null;
		} else {
			this.columnName = columnName.toUpperCase();
		}
	}
	
	public int getAdminId() {
		return adminId;
	}
	
	public void setAdminId(int adminId) {
		this.adminId = adminId;
	}
	
	public ProfileFieldMode getModeEdit() {
		return modeEdit;
	}
	
	public void setModeEdit(ProfileFieldMode modeEdit) {
		this.modeEdit = modeEdit;
	}
}
