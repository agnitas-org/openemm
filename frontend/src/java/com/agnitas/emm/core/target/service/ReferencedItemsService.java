/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.target.eql.referencecollector.ReferencedItemsCollection;

/**
 * Service to handle information on items referenced by target groups
 */
public interface ReferencedItemsService {

	/**
	 * Saves information on referenced items.
	 * Existing information is deleted prior to saving.
	 * 
	 * @param referencedObject information on referenced items
	 * @param companyID company ID of target group
	 * @param targetID ID of target group
	 */
	@Transactional
	void saveReferencedItems(final ReferencedItemsCollection referencedObject, final int companyID, final int targetID);

	/**
	 * Removes information on referenced items.
	 * 
	 * @param companyID company ID of target group
	 * @param targetID ID of target group
	 */
	@Transactional
	void removeReferencedItems(final int companyID, final int targetID);

	
	List<TargetLight> listTargetGroupsReferencingProfileFieldByVisibleName(final int companyID, final String visibleShortname);
	List<TargetLight> listTargetGroupsReferencingMailing(final int companyID, final int mailingID);
	List<TargetLight> listTargetGroupsReferencingLink(final int companyID, final int linkID);
	List<TargetLight> listTargetGroupsReferencingAutoImport(final int companyID, final int autoImportID);
	List<TargetLight> listTargetGroupsReferencingReferenceTable(final int companyID, final int tableID);
	List<TargetLight> listTargetGroupsReferencingReferenceTableColumn(final int companyID, final int tableID, final String columnName);
	
}
