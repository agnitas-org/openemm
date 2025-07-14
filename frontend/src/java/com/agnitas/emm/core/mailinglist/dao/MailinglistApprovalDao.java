/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.dao;

import java.util.Collection;
import java.util.List;

import com.agnitas.beans.Mailinglist;

//TODO: Used for emm only. Invalid scope. Code should be rafactored to change scope to the correct.
public interface MailinglistApprovalDao {

	/**
	 * Get all mailinglist names which can be used by the admin.
	 */
	List<Mailinglist> getEnabledMailinglistsNamesForAdmin(int companyId, int adminId);

	/**
	 * Get all mailinglist which can be used by the admin.
	 */
	List<Mailinglist> getEnabledMailinglistsForAdmin(int companyId, int adminId);

	/**
	 * Get all mailinglist which are not available for the admin.
	 */
    List<Integer> getDisabledMailinglistsForAdmin(int companyId, int adminId);

	/**
	 * Get all admins which have no access to mailinglist.
	 */
	List<Integer> getAdminsDisallowedToUseMailinglist(int companyId, int mailinglistId);

	/**
	 * Check if admin has access to certain mailing list.
	 */
	boolean isAdminHaveAccess(int companyId, int adminId, int mailingListId);

	/**
	 * Check if admin has any disabled mailing lists.
	 */
	boolean hasAnyDisabledMailingListsForAdmin(int companyId, int adminId);

	/**
	 * Check if admin has any disabled mailing lists binding for the recipient
	 * @param companyId 	company identifier
	 * @param adminId		admin identifier
	 * @param recipientId	recipient identifier
	 * @return true if admin has at least one disabled mailing list that bound to the recipient otherwise false
	 */
	boolean hasAnyDisabledRecipientBindingsForAdmin(int companyId, int adminId, int recipientId);

	/**
	 * Add rows to disabled_mailinglist_tbl for each mailinglist and chosen admin. <br>
	 * Each row means that admin has no access to mailinglist. <br>
	 * @return true if batch insert was successful
	 */
	boolean disallowAdminToUseMailinglists(int companyId, int adminID, Collection<Integer> mailinglistIds);

	/**
	 * Add rows to disabled_mailinglist_tbl for each admin and chosen mailinglist. <br>
	 * Each row means that admin has no access to mailinglist. <br>
	 * @return true if batch insert was successful
	 */
	boolean disallowAdminsToUseMailinglist(int companyId, int mailinglistId, Collection<Integer> adminIds);

	/**
	 * Removes rows from disabled_mailinglist_tbl with each mailinglist and chosen admin. <br>
	 * Each row means that admin has no access to mailinglist.
	 */
	void allowAdminToUseMailinglists(int companyId, int adminId, Collection<Integer> mailinglistIds);

	/**
	 * Removes rows from disabled_mailinglist_tbl with each admin and chosen mailinglist. <br>
	 * Each row means that admin has no access to mailinglist.
	 */
	void allowAdminsToUseMailinglist(int companyId, int mailinglistId, Collection<Integer> adminIds);

	/**
	 * Removes all rows from disabled_mailinglist_tbl with chosen admin. <br>
	 * Each row means that admin has no access to mailinglist.
	 */
	void allowAdminToUseAllMailinglists(int companyId, int adminId);

	/**
	 * Removes all rows from disabled_mailinglist_tbl with chosen mailinglist. <br>
	 * Each row means that admin has no access to mailinglist.
	 */
	void allowAllAdminsToUseMailinglist(int companyId, int mailinglistId);
	
	boolean deleteDisabledMailinglistsByCompany(int companyId);

	List<Integer> getMailinglistsWithMailinglistApproval(int companyId);
}
