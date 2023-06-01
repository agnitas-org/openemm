/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.List;
import java.util.Set;

import com.agnitas.emm.core.Permission;

/**
 * Bean representing an AdminGroup (mainly for User-Permissions)
 * The concept of permission is that every user with the permission to manage
 * userrights has the permission to manage the rights he got for all other
 * users.
 * This means that only a very limited number of user should be allowed to
 * manage userrights.
 */
public interface AdminGroup {
    /**
     * Getter for property companyID.
     * 
     * @return Value of property companyID
     */
    int getCompanyID();

    /**
     * Getter for property description.
     * 
     * @return The description of the AdminGroup.
     */
    String getDescription();

    /**
     * Getter for property groupID.
     * 
     * @return The id of the AdminGroup.
     */
    int getGroupID();

    /**
     * Getter for property groupPermissions.
     * Return a Set containing one entry for each permission, this group has.
     * 
     * @return A set of Permissions.
     */
    Set<Permission> getGroupPermissions();

    /**
     * Get the property Shortname.
     * The shortname is a short descriptive name for the group.
     * 
     * @return Value of property shortname
     */
    String getShortname();

    /**
     * Check if admingroup has any of the demanded permission rights.
     * @param permissions
     */
    boolean permissionAllowed(Permission... permissions);

    /**
     * Setter for property companyID.
     * 
     * @param id companyID
     */
    void setCompanyID(int id);

    /**
     * Setter for property description.
     * 
     * @param description New value of property description.
     */
    void setDescription(String description);

    /**
     * Setter for property groupID.
     * 
     * @param groupID New value of property groupID.
     */
    void setGroupID(int groupID);

    /**
     * Setter for property groupPermissions.
     * 
     * @param groupPermissions New value of property groupPermissions.
     */
    void setGroupPermissions(Set<Permission> groupPermissions);

    /**
     * Setter for property shortname.
     * 
     * @param name shortname
     */
    void setShortname(String name);

	void setCompanyPermissions(Set<Permission> companyPermissions);

	Set<AdminGroup> getParentGroups();

	void setParentGroups(Set<AdminGroup> parentGroups);

	List<Integer> getParentGroupIds();

	boolean permissionAllowedByParentGroups(Permission... permission);
}
