/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.Admin;

public interface MailinglistApprovalService {

	List<Mailinglist> getEnabledMailinglistsForAdmin(Admin admin);

	List<Mailinglist> getEnabledMailinglistsNamesForAdmin(Admin admin);
	
	Set<Integer> getAdminsAllowedToUseMailinglist(@VelocityCheck int companyId, int mailinglistId);

    /**
     * Disable all mailinglists in collection for admin. <br>
     * If collection is empty enable all mailinglists for admin. <br>
     * Works faster if mailinglistIDs is a set.
     * @param mailinglistIds set of mailinglist ids to disable for admin
     * @return were batch updates successful
     */
	boolean setDisabledMailinglistForAdmin(@VelocityCheck int companyId, int adminId, Collection<Integer> mailinglistIds);

    /**
     * Disable mailinglist for all admins in collection. <br>
     * If collection is empty enable mailinglist for all admins. <br>
     * Works faster if adminIDs is a set.
     * @param adminIds set of adminIDs ids to disable
     * @return were batch updates successful
     */
	boolean setAdminsDisallowedToUseMailinglist(@VelocityCheck int companyId, int mailinglistId, Collection<Integer> adminIds);

    /**
     * Get all mailinglist which are not available for the admin.
     */
	List<Integer> getDisabledMailinglistsForAdmin(@VelocityCheck int companyId, int adminId);

	/**
	 * Check is admin have access to certain mailing list.
	 */
	boolean isAdminHaveAccess(Admin admin, int mailingListId);

	/**
	 * Check is admin have any disabled mailing lists.
	 */
	boolean hasAnyDisabledMailingListsForAdmin(Admin admin);

	boolean hasAnyDisabledRecipientBindingsForAdmin(Admin admin, int recipientId);

	boolean hasAnyDisabledMailingListsForAdmin(@VelocityCheck int companyId, int adminId);

	boolean editUsersApprovalPermissions(int companyId, int mailinglistId, Set<Integer> allowedUserIds, List<UserAction> userActions);

	List<Integer> getMailinglistsWithMailinglistApproval(int companyId);

}
