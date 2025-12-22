/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Mailinglist;
import com.agnitas.dao.DaoUpdateReturnValueCheck;

public interface MailinglistDao {

	@DaoUpdateReturnValueCheck
	int updateMailinglist(int companyId, Mailinglist list);
	
	@DaoUpdateReturnValueCheck
	int createMailinglist(int companyId, Mailinglist list);
	
	/**
	 * Deletes mailinglist from database.
	 * 
	 * @param listID The id of the mailing list to delete.
	 * @param companyId The id of mailing list company.
	 * @return true on success.
	 */
	boolean deleteMailinglist(int listID, int companyId);

	/**
	 * Loads mailing list identified by list id and company id.
	 * 
	 * @param listID The id of the mailing list that should be loaded.
	 * @param companyId The companyID for the mailing list.
	 * @return The Mailinglist or null on failure or if companyID is 0.
	 */
	Mailinglist getMailinglist(int listID, int companyId);

	/**
	 * Loads a shortname of a referenced mailinglist.
	 *
	 * @param mailinglistId The id of a mailing list.
	 * @param companyId The id of mailing list company.
	 * @return a shortname of a referenced mailing list or null.
	 */
	String getMailinglistName(int mailinglistId, int companyId);

	/**
	 * Loads all mailing lists for company id.
	 * 
	 * @param companyId The companyID for the mailing lists.
	 * @return List of Mailinglists or empty list.
	 */
	List<Mailinglist> getMailinglists(int companyId);

	/**
	 * Saves or updates mailinglist.
	 * 
	 * @param list The mailing list to save.
	 * @return Saved mailinglist id.
	 */
	int saveMailinglist(Mailinglist list);

	int countSubscribers(final int mailinglistId, final int companyId, final int targetId, final boolean includeWorldRecipients, final boolean includeAdminRecipients, final boolean includeTestRecipients, final Set<Integer> bindingStates);

	int getNumberOfActiveTestSubscribers(int targetId, int companyId, int id);

	int getNumberOfActiveAdminSubscribers(int targetId, int companyId, int id);

	int getNumberOfActiveWorldSubscribers(int targetId, int companyId, int id);

	/**
	 * Checks if mailing list with given name exists.
	 * 
	 * @param mailinglistName The name of mailing list for check.
	 * @param companyId The company id for mailing list.
	 * @return true if the mailing list exists, and false otherwise
	 */
	boolean mailinglistExists(String mailinglistName, int companyId);

	/**
	 * Checks if mailing list with given id exists.
	 * 
	 * @param mailinglistId The mailing list id for check.
	 * @param companyId The company id for mailing list.
	 * @return true if the mailing list exists, and false otherwise
	 */
	boolean exist(int mailinglistId, int companyId);

	int getCountOfMailinglists(int companyId);

    List<Integer> getMailinglistIds(int companyId);

	boolean deleteAllMailinglist(int companyId);

	List<Mailinglist> getMailingListsNames(int companyId);

	Map<Integer, Integer> getMailinglistWorldSubscribersStatistics(int companyId, int mailinglistID);

    boolean mailinglistDeleted(int mailinglistId, int companyId);

    Mailinglist getDeletedMailinglist(int mailinglistId, int companyId);

	List<Mailinglist> getMailinglists(int companyId, int adminID);

}
