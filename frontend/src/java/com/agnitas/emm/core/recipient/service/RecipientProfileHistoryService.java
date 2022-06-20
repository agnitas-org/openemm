/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.service;

import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.emm.core.recipient.RecipientProfileHistoryException;

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
	default void enableProfileFieldHistory(@VelocityCheck int companyId) throws RecipientProfileHistoryException {
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
	 * @param companyId company ID
	 * @throws RecipientProfileHistoryException on errors during processing the request
	 */
	void afterProfileFieldStructureModification(@VelocityCheck int companyId) throws RecipientProfileHistoryException;
	
	/**
	 * Returns <code>true</code>, if history feature is enabled for given company ID.
	 * 
	 * @param companyId company ID
	 * 
	 * @return <code>true</code> if history feature is enabled
	 */
	boolean isProfileFieldHistoryEnabled(@VelocityCheck int companyId);
	
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
	List<ComRecipientHistory> listProfileFieldHistory(final int subscriberID, @VelocityCheck int companyId) throws RecipientProfileHistoryException;
	
	@SuppressWarnings("unused")
	default void disableProfileFieldHistory(@VelocityCheck int companyId) throws RecipientProfileHistoryException {
		// default implementation
	}
}
