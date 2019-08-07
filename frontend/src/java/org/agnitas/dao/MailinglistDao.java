/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.List;

import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mailinglist.bean.MailinglistEntry;

public interface MailinglistDao {
	/**
	 * Check if there is at least one mailing that uses the mailing list referenced by {@code mailinglistId}.
	 *
	 * @param mailinglistId The id of the mailing list to check.
	 * @param companyId The id of a company that owns the mailing list.
	 * @return {@code true} if the mailing list is in use of {@code false} otherwise.
	 */
	boolean checkMailinglistInUse(int mailinglistId, @VelocityCheck int companyId);
	
	@DaoUpdateReturnValueCheck
	int updateMailinglist(@VelocityCheck int companyId, Mailinglist list);
	
	
	@DaoUpdateReturnValueCheck
	int createMailinglist(@VelocityCheck int companyId, Mailinglist list);
	
	/**
	 * Deletes mailinglist from database.
	 * 
	 * @param listID The id of the mailing list to delete.
	 * @param companyId The id of mailing list company.
	 * @return true on success.
	 */
	boolean deleteMailinglist(int listID, @VelocityCheck int companyId);

	/**
	 * Loads mailing list identified by list id and company id.
	 * 
	 * @param listID The id of the mailing list that should be loaded.
	 * @param companyId The companyID for the mailing list.
	 * @return The Mailinglist or null on failure or if companyID is 0.
	 */
	Mailinglist getMailinglist(int listID, @VelocityCheck int companyId);

	/**
	 * Loads a shortname of a referenced mailinglist.
	 *
	 * @param mailinglistId The id of a mailing list.
	 * @param companyId The id of mailing list company.
	 * @return a shortname of a referenced mailing list or null.
	 */
	String getMailinglistName(int mailinglistId, @VelocityCheck int companyId);

	/**
	 * Loads all mailing lists for company id.
	 * 
	 * @param companyId The companyID for the mailing lists.
	 * @return List of Mailinglists or empty list.
	 */
	List<Mailinglist> getMailinglists(@VelocityCheck int companyId);

	/**
	 * Saves or updates mailinglist.
	 * 
	 * @param list The mailing list to save.
	 * @return Saved mailinglist id.
	 */
	int saveMailinglist(Mailinglist list);

	/**
	 * Deletes all bindings for mailing list.
	 * 
	 * @param id The id of mailing list.
	 * @param companyId The company id for bindings.
	 * @return {@code true} if all the binding were deleted, and {@code false} if there were no bindings
	 * or something went wrong.
	 */
	boolean deleteBindings(int id, @VelocityCheck int companyId);

	/**
	 * Get numbers of recipients related to given mailing list.
	 * 
	 * @param admin Include admin recipients.
	 * @param test Include test recipients.
	 * @param world Include normal recipients.
	 * @param targetId Id of target group.
	 * @param companyId The company id for recipients.
	 * @param id
	 * @return number of active recipients for mailing list.
	 */
	int getNumberOfActiveSubscribers(boolean admin, boolean test, boolean world, int targetId,
									 @VelocityCheck int companyId, int id);

	/**
	 * Checks if mailing list with given name exists.
	 * 
	 * @param mailinglistName The name of mailing list for check.
	 * @param companyId The company id for mailing list.
	 * @return true if the mailing list exists, and false otherwise
	 */
	boolean mailinglistExists(String mailinglistName, @VelocityCheck int companyId);

	/**
	 * Checks if mailing list with given id exists.
	 * 
	 * @param mailinglistId The mailing list id for check.
	 * @param companyId The company id for mailing list.
	 * @return true if the mailing list exists, and false otherwise
	 */
	boolean exist(int mailinglistId, @VelocityCheck int companyId);

	PaginatedListImpl<MailinglistEntry> getMailinglists(@VelocityCheck int companyId, int adminId, String sort, String direction, int page, int rownums);

	boolean deleteAllMailinglist(@VelocityCheck int companyId);

	List<Mailinglist> getMailingListsNames(@VelocityCheck int companyId);

}
