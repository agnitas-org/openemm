/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dao;

import java.util.List;

import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.emm.core.recipient.RecipientProfileHistoryException;

/**
 * Persistence-layer interface for writing history data of modified profile fields.
 */
public interface RecipientProfileHistoryDao {
	/**
	 * Sets up profile field history for given company ID.
	 *
	 * @param companyID     company ID
	 * @param profileFields list of profile fields to be included in history
	 * @throws RecipientProfileHistoryException on errors processing request
	 */
	default void setupProfileHistory(final int companyID, final List<ProfileField> profileFields) throws RecipientProfileHistoryException {
		// default implementation
	}
	
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
	 * In case of database persistence, this method could rebuild triggers, etc.
	 * 
	 * @param companyID company ID
	 * @param profileFields list of profile fields to be included in history
	 * 
	 * @throws RecipientProfileHistoryException on errors during processing the request
	 */
	void afterProfileFieldStructureModification(final int companyID, final List<ProfileField> profileFields) throws RecipientProfileHistoryException;

	/**
	 * Lists all profile fields modifications for given subscriber.
	 * The list is sorted ascending by timestamp.
	 * 
	 * @param subscriberID subscriber ID
	 * @param companyID company ID
	 * @return empty list in case of table not exist or nothing for retrieving
	 * otherwise return List with recipient profile changes.
	 */
	List<ComRecipientHistory> listProfileFieldHistory(final int subscriberID, final int companyID);
	
	default void deactivateProfileHistory(int companyID) {
		// default implementation
	}
}
