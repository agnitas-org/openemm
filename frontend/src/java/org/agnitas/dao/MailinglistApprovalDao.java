/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.Collection;
import java.util.List;

import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.velocity.VelocityCheck;

//TODO: Used for emm only. Invalid scope. Code should be rafactored to change scope to the correct.
public interface MailinglistApprovalDao {

	/**
	 * Get all mailinglist names which can be used by the admin.
	 */
	List<Mailinglist> getEnabledMailinglistsNamesForAdmin(@VelocityCheck int companyId, int adminId);

	/**
	 * Get all mailinglist which can be used by the admin.
	 */
	List<Mailinglist> getEnabledMailinglistsForAdmin(@VelocityCheck int companyId, int adminId);

	/**
	 * Get all mailinglist which are not available for the admin.
	 */
    List<Integer> getDisabledMailinglistsForAdmin(@VelocityCheck int companyId, int adminId);

	/**
	 * Get all admins which have no access to mailinglist.
	 */
	List<Integer> getAdminsDisallowedToUseMailinglist(@VelocityCheck int companyId, int mailinglistId);

	/**
	 * Check if admin has access to certain mailing list.
	 */
	boolean isAdminHaveAccess(@VelocityCheck int companyId, int adminId, int mailingListId);

	/**
	 * Check if admin has any disabled mailing lists.
	 */
	boolean hasAnyDisabledMailingListsForAdmin(@VelocityCheck int companyId, int adminId);

	/**
	 * Check if admin has any disabled mailing lists binding for the recipient
	 * @param companyId 	company identifier
	 * @param adminId		admin identifier
	 * @param recipientId	recipient identifier
	 * @return true if admin has at least one disabled mailing list that bound to the recipient otherwise false
	 */
	boolean hasAnyDisabledRecipientBindingsForAdmin(@VelocityCheck int companyId, int adminId, int recipientId);

	/**
	 * Add rows to disabled_mailinglist_tbl for each mailinglist and chosen admin. <br>
	 * Each row means that admin has no access to mailinglist. <br>
	 * @return true if batch insert was successful
	 */
	boolean disallowAdminToUseMailinglists(@VelocityCheck int companyId, int adminID, Collection<Integer> mailinglistIds);

	/**
	 * Add rows to disabled_mailinglist_tbl for each admin and chosen mailinglist. <br>
	 * Each row means that admin has no access to mailinglist. <br>
	 * @return true if batch insert was successful
	 */
	boolean disallowAdminsToUseMailinglist(@VelocityCheck int companyId, int mailinglistId, Collection<Integer> adminIds);

	/**
	 * Removes rows from disabled_mailinglist_tbl with each mailinglist and chosen admin. <br>
	 * Each row means that admin has no access to mailinglist.
	 */
	void allowAdminToUseMailinglists(@VelocityCheck int companyId, int adminId, Collection<Integer> mailinglistIds);

	/**
	 * Removes rows from disabled_mailinglist_tbl with each admin and chosen mailinglist. <br>
	 * Each row means that admin has no access to mailinglist.
	 */
	void allowAdminsToUseMailinglist(@VelocityCheck int companyId, int mailinglistId, Collection<Integer> adminIds);

	/**
	 * Removes all rows from disabled_mailinglist_tbl with chosen admin. <br>
	 * Each row means that admin has no access to mailinglist.
	 */
	void allowAdminToUseAllMailinglists(@VelocityCheck int companyId, int adminId);

	/**
	 * Removes all rows from disabled_mailinglist_tbl with chosen mailinglist. <br>
	 * Each row means that admin has no access to mailinglist.
	 */
	void allowAllAdminsToUseMailinglist(@VelocityCheck int companyId, int mailinglistId);

	List<Integer> getMailinglistsWithMailinglistApproval(int companyId);
}
