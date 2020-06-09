/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.beans.impl;

import java.util.Set;

import org.agnitas.beans.AdminGroup;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.emm.core.Permission;

public class AdminGroupImpl implements AdminGroup {
	protected int companyID;
	protected String shortname;

	protected Set<Permission> groupPermissions;
	protected Set<Permission> companyPermissions;

	protected int groupID = -1;

	/**
	 * Holds value of property description.
	 */
	protected String description;

	// * * * * *
	// SETTER:
	// * * * * *
	@Override
	public void setCompanyID( @VelocityCheck int id) {
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
	public boolean permissionAllowed(int companyIdToCheckFor, Permission... permissions) {
		return Permission.permissionAllowed(companyIdToCheckFor, groupPermissions, companyPermissions, permissions);
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
}
