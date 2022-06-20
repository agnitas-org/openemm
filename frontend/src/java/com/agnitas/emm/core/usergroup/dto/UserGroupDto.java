/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.dto;

import java.util.HashSet;
import java.util.Set;

public class UserGroupDto {
    
    private int id;
    
    private int companyId;
    
    private String shortname;
    
    private String description;
    
    private Set<String> grantedPermissions;
    
    private String[] parentGroupIDs;

    public int getUserGroupId() {
        return id;
    }
    
    public void setUserGroupId(int userGroupId) {
        this.id = userGroupId;
    }
    
    public int getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }
    
    public String getShortname() {
        return shortname;
    }
    
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getGrantedPermissions() {
        return grantedPermissions == null ? new HashSet<>() : grantedPermissions;
    }
    
    public void setGrantedPermissions(Set<String> grantedPermissions) {
        this.grantedPermissions = grantedPermissions;
    }

	public String[] getParentGroupIDs() {
		return parentGroupIDs;
	}

	public void setParentGroupIDs(String[] parentGroupIDs) {
		this.parentGroupIDs = parentGroupIDs;
	}
}
