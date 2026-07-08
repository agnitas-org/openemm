/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.dto;

import java.util.HashSet;
import java.util.Set;

@Deprecated // TODO Replace by WebserviceUser
public class WebserviceUserDto {

    private String userName;

    private int companyId;

    private String password;

    private String email;

    private boolean active;
    
    private Set<String> grantedPermissions;
    private Set<Integer> grantedPermissionGroups;
    
    public WebserviceUserDto() {
    	setGrantedPermissions(null);
    	setGrantedPermissionGroupIDs(null);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

	public final void setGrantedPermissions(final Set<String> permissions) {
		this.grantedPermissions = permissions != null ? permissions : new HashSet<>();
	}
	
	public final Set<String> getGrantedPermissions() {
		return this.grantedPermissions;
	}
	
	public final void setGrantedPermissionGroupIDs(final Set<Integer> permissionGroups) {
		this.grantedPermissionGroups = permissionGroups != null ? permissionGroups : new HashSet<>();
	}
	
	public final Set<Integer> getGrantedPermissionGroupIDs() {
		return this.grantedPermissionGroups;
	}
}
