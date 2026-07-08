/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.dao;

import java.util.List;

import com.agnitas.beans.TargetLight;

/**
 * DAO to store objects referenced by target groups in lists.
 */
public interface ReferencedItemsDao {
	
	/**
	 * Removes all referenced objects for given target group from list.
	 *  
	 * @param companyID company ID of target group
	 * @param targetID ID of target group
	 */
    void removeAllReferencedObjects(final int companyID, final int targetID);
	
	/**
	 * Saves the list of referenced mailing for given target group.
	 * 
	 * @param companyID company ID of target group
	 * @param targetID ID of target group
	 * @param ids list of IDs of referenced mailings
	 */
    void saveReferencedMailings(final int companyID, final int targetID, final List<Integer> ids);

	/**
	 * Saves the list of referenced links for given target group.
	 * 
	 * @param companyID company ID of target group
	 * @param targetID ID of target group
	 * @param ids list of IDs of referenced links
	 */
    void saveReferencedLinks(final int companyID, final int targetID, final List<Integer> ids);
	
	/**
	 * Saves the list of referenced auto-imports for given target group.
	 * 
	 * @param companyID company ID of target group
	 * @param targetID ID of target group
	 * @param ids list of IDs of referenced auto-imports
	 */
    void saveReferencedAutoImports(final int companyID, final int targetID, final List<Integer> ids);
	
	/**
	 * Saves the list of referenced profile fields for given target group.
	 * 
	 * @param companyID company ID of target group
	 * @param targetID ID of target group
	 * @param profileFieldNames list of names of referenced profile fields
	 */
    void saveReferencedProfileFields(final int companyID, final int targetID, final List<String> profileFieldNames);

	/**
	 * Lists all target groups of company referencing profile field of given name.
	 * 
	 * @param companyID company ID
	 * @param visibleShortname visible name of profile field (not DB name)
	 * 
	 * @return (empty) list of target groups referencing given profile field
	 */
	List<TargetLight> listTargetGroupsReferencingProfileField(final int companyID, final String visibleShortname);

	/**
	 * Lists all target groups of company referencing given mailing.
	 * 
	 * @param companyID company ID
	 * @param mailingID mailing ID
	 * 
	 * @return (empty) list of target groups referencing given mailing
	 */
	List<TargetLight> listTargetGroupsReferencingMailing(final int companyID, final int mailingID);

	/**
	 * Lists all target groups of company referencing given auto import.
	 * 
	 * @param companyID company ID
	 * @param autoImportID ID of auto import
	 * 
	 * @return (empty) list of target groups referencing given auto import
	 */
    List<TargetLight> listTargetGroupsReferencingAutoImport(final int companyID, final int autoImportID);
}
