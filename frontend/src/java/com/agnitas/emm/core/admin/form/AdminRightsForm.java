/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.form;

import java.util.Set;

public class AdminRightsForm {

    private String username;
    private int adminID = 0;
    private Set<String> userRights;
    private Set<String> grantedPermissions;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }

    public Set<String> getUserRights() {
        return userRights;
    }

    public void setUserRights(Set<String> userRights) {
        this.userRights = userRights;
    }

    public Set<String> getGrantedPermissions() {
        return grantedPermissions;
    }

    public void setGrantedPermissions(Set<String> grantedPermissions) {
        this.grantedPermissions = grantedPermissions;
    }
}
