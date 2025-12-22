/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;


import com.agnitas.beans.RecipientHistory;
import com.agnitas.emm.core.recipient.exception.RecipientProfileHistoryException;

/**
 * Service-layer interface for writing history data of profile field modifications.
 */
public interface RecipientProfileHistoryService {
	/**
	 * Enables profile field history.
	 *
	 * @param companyId company ID
	 * @throws RecipientProfileHistoryException on errors enabling profile field history
	 */
	void enableProfileFieldHistory(int companyId) throws RecipientProfileHistoryException;
	
	/**
	 * This method is called, when the structure of the profile fields has been changed.
	 * 
	 * This changes are
	 * <ul>
	 *   <li>Adding a new profile field</li>
	 *   <li>Removing a profile field</li>
	 *   <li>Changing the type of a profile field</li>
	 *   <li>Changing the <i>history</li>-flag of a profile field</li>
	 * </ul>  
	 * 
	 * @param companyId company ID
	 * @throws RecipientProfileHistoryException on errors during processing the request
	 */
	void afterProfileFieldStructureModification(int companyId) throws RecipientProfileHistoryException;

	/**
	 * Finds recipient IDs with profile field changes after the given time.
	 *
	 * @param fields     profile field names to check for changes
	 * @param from       timestamp from which to start checking for changes
	 * @param companyId  ID of the company to filter recipients by
	 * @return list of recipient IDs with changed profile fields since the specified time
	 */
	List<Integer> getChangedRecipients(Set<String> fields, ZonedDateTime from, int companyId);

	/**
	 * Returns <code>true</code>, if history feature is enabled for given company ID.
	 * 
	 * @param companyId company ID
	 * 
	 * @return <code>true</code> if history feature is enabled
	 */
	boolean isProfileFieldHistoryEnabled(int companyId);
	
	/**
	 * Lists all profile fields modifications for given subscriber.
	 * The list is sorted ascending by timestamp.
	 * 
	 * @param subscriberID subscriber ID
	 * @param companyId company ID
	 * 
	 * @return List of profile fields modifications
	 * 
	 * @throws RecipientProfileHistoryException on errors reading history data
	 */
	List<RecipientHistory> listProfileFieldHistory(final int subscriberID, int companyId) throws RecipientProfileHistoryException;
	
	void disableProfileFieldHistory(int companyId) throws RecipientProfileHistoryException;
}
