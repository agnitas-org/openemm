/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.beans.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.agnitas.beans.AdminGroup;
import com.agnitas.emm.core.Permission;

public class AdminGroupImpl implements AdminGroup {

	protected int companyID;
	protected String shortname;
    protected String companyName;

	protected Set<Permission> groupPermissions;
	protected Set<Permission> companyPermissions;
	
	protected Set<AdminGroup> parentGroups;

	protected int groupID = -1;

	/**
	 * Holds value of property description.
	 */
	protected String description;

	// * * * * *
	// SETTER:
	// * * * * *
	@Override
	public void setCompanyID(int id) {
		companyID = id;
	}

	@Override
	public void setShortname(String name) {
		shortname = name;
	}

	@Override
	public int getCompanyID() {
		return companyID;
	}

	@Override
	public String getShortname() {
		return shortname;
	}

    @Override
    public String getCompanyName() {
        return companyName;
    }

    @Override
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
	public int getGroupID() {
		return groupID;
	}

	/**
	 * Setter for property groupID.
	 * 
	 * @param groupID
	 *            New value of property groupID.
	 */
	@Override
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	@Override
	public Set<Permission> getGroupPermissions() {
		return groupPermissions;
	}

	@Override
	public void setGroupPermissions(Set<Permission> groupPermissions) {
		this.groupPermissions = groupPermissions;
	}

	@Override
	public boolean permissionAllowed(Permission... permissions) {
		if (Permission.permissionAllowed(groupPermissions, companyPermissions, permissions)){
			return true;
		} else {
			return permissionAllowedByParentGroups(permissions);
		}
	}

	/**
	 * Getter for property description.
	 * 
	 * @return Value of property description.
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Setter for property description.
	 * 
	 * @param description
	 *            New value of property description.
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setCompanyPermissions(Set<Permission> companyPermissions) {
		this.companyPermissions = companyPermissions;
	}

	@Override
	public Set<AdminGroup> getParentGroups() {
		return parentGroups;
	}

	@Override
	public void setParentGroups(Set<AdminGroup> parentGroups) {
		this.parentGroups = parentGroups;
	}

	@Override
	public List<Integer> getParentGroupIds() {
		List<Integer> groupIds = new ArrayList<>();
	    if (getParentGroups() != null && !getParentGroups().isEmpty()) {
	    	for (AdminGroup group : getParentGroups()) {
	    		groupIds.add(group.getGroupID());
	    	}
	    }
	    return groupIds;
	}

	@Override
	public boolean permissionAllowedByParentGroups(Permission... permission) {
		if (parentGroups != null && !parentGroups.isEmpty() ) {
			for (AdminGroup adminGroup : parentGroups) {
				if (adminGroup.permissionAllowed(permission)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}
}
