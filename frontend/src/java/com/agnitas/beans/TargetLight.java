/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;
import java.util.regex.Pattern;

import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Interface for lightweight target group objects.
 * 
 * This interface provides access to basic information about target groups,
 * but not structural information.
 */
public interface TargetLight {
	String LIST_SPLIT_PREFIX = "__listsplit_";
	String LIST_SPLIT_CM_PREFIX = "__WM_listsplit_";

	Pattern LIST_SPLIT_PATTERN = Pattern.compile("(?:\\d\\d)+_\\d");
	Pattern LIST_SPLIT_CM_PATTERN = Pattern.compile("(?:\\d+\\.\\d)(?:;\\d+\\.\\d)*_\\d");

	String[] LIST_SPLIT_PREFIXES = {
			LIST_SPLIT_PREFIX,
			LIST_SPLIT_CM_PREFIX
	};

	/**
	 * Returns company ID of target group.
	 * 
	 * @return company ID of target group
	 */
	int getCompanyID();
	
	/**
	 * Sets company ID of target group.
	 * 
	 * @param companyID company ID of target group
	 */
	void setCompanyID(@VelocityCheck int companyID);
	
	/**
	 * Returns ID of target group.
	 * 
	 * @return ID of target group
	 */
	int getId();
	
	/**
	 * Sets ID of target group.
	 * 
	 * @param id ID of target group
	 */
	void setId(int id);
	
	/**
	 * Returns name of target group.
	 *  
	 * @return name of target group
	 */
	String getTargetName();
	
	/**
	 * Sets name of target group.
	 * 
	 * @param name name of target group
	 */
	void setTargetName(String name);
	
	/**
	 * Returns description.
	 * 
	 * @return description
	 */
	String getTargetDescription();

	/**
	 * Sets description.
	 * 
	 * @param description description
	 */
	void setTargetDescription(String description);

	/**
	 * Returns true, if target group is locked (write-protected).
	 * 
	 * @return true, if target group is locked (write-protected)
	 */
	boolean isLocked();

	/**
	 * Set write-protection flag of target group (true: write protected).
	 * 
	 * @param locked write-protection flag
	 */
	void setLocked(boolean locked);

	/**
	 * Returns creation date of target group.
	 * 
	 * @return creation date of target group
	 */
	Date getCreationDate();

	/**
	 * Sets creation date of target group.
	 * 
	 * @param creationDate creation date of target group
	 */
	void setCreationDate(Date creationDate);

	/**
	 * Returns date of last modification of target group.
	 * 
	 * @return date of last modification of target group
	 */
	Date getChangeDate();

	/**
	 * Sets date of last modification of target group.
	 * 
	 * @param changeDate date of last modification of target group
	 */
	void setChangeDate(Date changeDate);

    // TODO: Make "deleted" boolean, if Hibernate is not longer used
    void setDeleted(int deleted);
    
    // TODO: Make return type boolean, if Hibernate is not longer used
    int getDeleted();
    
    /**
     * Returns true, if target group is valid (no errors during saving like unknown profile fields, ...).
     * 
     * @return true, if target group is valid.
     */
    boolean isValid();
    
    /**
     * Sets validity flag of target group.
     *
     * @param valid true, if target group is valid.
     *
     * @see #isValid()
     */
    void setValid(boolean valid);

    /**
     * Checks, if target group is defined as a list split by the workflow manager.
     * 
     * @return <code>true</code> if target group is a workflow manager list split
     */
	boolean isWorkflowManagerListSplit();

	/**
	 * Checks, if a target name has a specific prefix (see {@link #LIST_SPLIT_PREFIXES}).
	 *
	 * @return {@code true} if the target represents list split.
     */
	boolean isListSplit();

	/**
	 * Create a {@link com.agnitas.beans.ListSplit} entity (available for valid list split targets only) out of the target.
	 *
	 * @return a {@link com.agnitas.beans.ListSplit} entity representing the target or {@code null} if validation fails.
     */
	ListSplit toListSplit();

    /**
     * Set to <code>true</code>, if target group can be used with content blocks. If not, set to <code>false</code>.
     * 
     * @param flag <code>true</code> if target group can be used with content blocks
     */
    void setComponentHide(final boolean flag);

    /**
     * Returns <code>true</code>, if target group can be used with content blocks. If not, <code>false</code> is returned.
     * 
     * @return <code>true</code> if target group can be used with content blocks
     */
    boolean getComponentHide();

	int getComplexityIndex();

	void setComplexityIndex(int complexityIndex);

	void setAccessLimitation(boolean accessLimitation);

	boolean isAccessLimitation();
	
	boolean isFavorite();
	
	void setFavorite(boolean favorite);
}
