/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.form;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserGroupForm {
    
    private int id;
    
    private int companyId;
    
    @NotNull(message = "error.name.is.empty")
    @Size.List({
        @Size(min = 3, message = "error.name.too.short"),
        @Size(max = 100, message = "error.username.tooLong")
    })
    private String shortname;
    
    private String description;
    
    private Set<String> grantedUserPermissions = new HashSet<>();
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public Set<String> getGrantedUserPermissions() {
        return grantedUserPermissions;
    }
    
    public void setGrantedUserPermissions(Set<String> grantedUserPermissions) {
        this.grantedUserPermissions = grantedUserPermissions;
    }
}
