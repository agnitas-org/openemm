/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.form;

import java.util.HashMap;
import java.util.Map;

public class WebserviceUserForm {

    private String userName;

    private String email;

    private String password;

    private int companyId;

    private boolean active;
    
    private final Map<String, String> endpointPermissions = new HashMap<>();
    private final Map<Integer, String> grantedPermissionGroups = new HashMap<>();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
     
    public final void setEndpointPermission(final Map<String, String> map) {
    	this.endpointPermissions.clear();
    	this.endpointPermissions.putAll(map);
    }
    
    public final Map<String, String> getEndpointPermission() {
    	return this.endpointPermissions;
    }
    
    public final void setPermissionGroups(final Map<Integer, String> map) {
    	this.grantedPermissionGroups.clear();
    	this.grantedPermissionGroups.putAll(map);
    }
    
    public final Map<Integer,String> getPermissionGroup() {
    	return this.grantedPermissionGroups;
    }
}
