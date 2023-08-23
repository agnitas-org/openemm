/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.MailinglistApprovalDao;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;

public class MailinglistApprovalServiceImpl implements MailinglistApprovalService {

	private final MailinglistApprovalDao mailinglistApprovalDao;
	private final AdminService adminService;
	private final MailinglistDao mailinglistDao;

    public MailinglistApprovalServiceImpl(MailinglistApprovalDao mailinglistApprovalDao, AdminService adminService, MailinglistDao mailinglistDao) {
        this.mailinglistApprovalDao = mailinglistApprovalDao;
        this.adminService = adminService;
        this.mailinglistDao = mailinglistDao;
    }

    @Override
    public List<Mailinglist> getEnabledMailinglistsNamesForAdmin(Admin admin) {
        return mailinglistApprovalDao.getEnabledMailinglistsNamesForAdmin(admin.getCompanyID(), admin.getAdminID());
    }

    @Override
    public List<Mailinglist> getEnabledMailinglistsForAdmin(Admin admin) {
        if (admin == null) {
            return new ArrayList<>();
        }

        if (mailinglistApprovalDao.hasAnyDisabledMailingListsForAdmin(admin.getCompanyID(), admin.getAdminID())) {
            return mailinglistApprovalDao.getEnabledMailinglistsForAdmin(admin.getCompanyID(), admin.getAdminID());
        } else {
            return mailinglistDao.getMailinglists(admin.getCompanyID());
        }
    }

    @Override
    public boolean setDisabledMailinglistForAdmin(int companyId, int adminId, Collection<Integer> mailinglistIds){
        return setDisabledMailinglistForAdmin(companyId, adminId, mailinglistIds, Optional.empty());
    }

    @Override
    public boolean setDisabledMailinglistForAdmin(int companyId, int adminId, Collection<Integer> mailinglistIds, Optional<List<UserAction>> userActions) {
        if (CollectionUtils.isEmpty(mailinglistIds)) {
            if (userActions.isPresent()) {
                List<Integer> alreadyDisabled = mailinglistApprovalDao.getDisabledMailinglistsForAdmin(companyId, adminId);
                addUserActionOfChangeMailinglistAccess(alreadyDisabled, userActions.get(), true, adminId);
            }

            mailinglistApprovalDao.allowAdminToUseAllMailinglists(companyId, adminId);
            return true;
        }

        List<Integer> alreadyDisabledList = mailinglistApprovalDao.getDisabledMailinglistsForAdmin(companyId, adminId);
        Set<Integer> alreadyDisabled = new HashSet<>(alreadyDisabledList);

        Collection<Integer> mailinglistsToEnable = CollectionUtils.removeAll(alreadyDisabled, mailinglistIds);
        Collection<Integer> mailinglistsToDisable = CollectionUtils.removeAll(mailinglistIds, alreadyDisabled);

        mailinglistApprovalDao.allowAdminToUseMailinglists(companyId, adminId, mailinglistsToEnable);
        boolean isSuccessful = mailinglistApprovalDao.disallowAdminToUseMailinglists(companyId, adminId, mailinglistsToDisable);

        if (userActions.isPresent()) {
            addUserActionOfChangeMailinglistAccess(mailinglistsToEnable, userActions.get(), true, adminId);

            if (isSuccessful) {
                addUserActionOfChangeMailinglistAccess(mailinglistsToDisable, userActions.get(), false, adminId);
            }
        }

        return isSuccessful;
    }

    private void addUserActionOfChangeMailinglistAccess(Collection<Integer> items, List<UserAction> userActions, boolean isGrantingAccess, int adminId) {
	    if (items.isEmpty()) {
	        return;
        }

        String mailinglistsAsStr = items.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

	    String accessType;
	    if (isGrantingAccess) {
            accessType = "Allowed";
        } else {
            accessType = "Disallowed";
        }

        String description = String.format("%s mailing lists (%s) for admin: %d", accessType, mailinglistsAsStr, adminId);
        userActions.add(new UserAction("edit user", description));
    }

    @Override
    public boolean setAdminsDisallowedToUseMailinglist(int companyId, int mailinglistId, Collection<Integer> adminIds){
        if(CollectionUtils.isEmpty(adminIds)){
        	mailinglistApprovalDao.allowAllAdminsToUseMailinglist(companyId, mailinglistId);
            return true;
        }

        List<Integer> alreadyDisabledList = mailinglistApprovalDao.getAdminsDisallowedToUseMailinglist(companyId, mailinglistId);

        if(CollectionUtils.isEmpty(alreadyDisabledList)){
            return mailinglistApprovalDao.disallowAdminsToUseMailinglist(companyId, mailinglistId, adminIds);
        }

        Set<Integer> alreadyDisabled = new HashSet<>(alreadyDisabledList);
        Collection<Integer> enableUsers = CollectionUtils.removeAll(alreadyDisabled, adminIds);
        Collection<Integer> disableUsers = CollectionUtils.removeAll(adminIds, alreadyDisabled);

        mailinglistApprovalDao.allowAdminsToUseMailinglist(companyId, mailinglistId, enableUsers);
        return mailinglistApprovalDao.disallowAdminsToUseMailinglist(companyId, mailinglistId, disableUsers);
    }

    @Override
    public List<Integer> getDisabledMailinglistsForAdmin(int companyId, int adminId){
        return mailinglistApprovalDao.getDisabledMailinglistsForAdmin(companyId, adminId);
    }

    @Override
    public boolean isAdminHaveAccess(Admin admin, int mailingListId) {
        return mailingListId <= 0 || mailinglistApprovalDao.isAdminHaveAccess(admin.getCompanyID(), admin.getAdminID(), mailingListId);
    }
    
    @Override
    public boolean hasAnyDisabledMailingListsForAdmin(Admin admin) {
        return hasAnyDisabledMailingListsForAdmin(admin.getCompanyID(), admin.getAdminID());
    }

    @Override
    public boolean hasAnyDisabledRecipientBindingsForAdmin(Admin admin, int recipientId) {
        return admin != null && mailinglistApprovalDao.hasAnyDisabledRecipientBindingsForAdmin(admin.getCompanyID(), admin.getAdminID(), recipientId);
    }

    @Override
    public boolean hasAnyDisabledMailingListsForAdmin(int companyId, int adminId) {
        return companyId > 0 && adminId > 0 && mailinglistApprovalDao.hasAnyDisabledMailingListsForAdmin(companyId, adminId);
    }

    @Override
    public Set<Integer> getAdminsAllowedToUseMailinglist(int companyId, int mailinglistId){
        List<Integer> adminsDisallowedToUseMailinglist = mailinglistApprovalDao.getAdminsDisallowedToUseMailinglist(companyId, mailinglistId);
        Set<Integer> adminIds= adminService.getAdminsNamesMap(companyId).keySet();
        Collection<Integer> allowed = adminIds;
        if (!adminsDisallowedToUseMailinglist.isEmpty()) {
            allowed = CollectionUtils.removeAll(adminIds, adminsDisallowedToUseMailinglist);
        }
        return new HashSet<>(allowed);
    }
    @Override
    public boolean editUsersApprovalPermissions(int companyId, int mailinglistId, Set<Integer> allowedUserIds, List<UserAction> userActions) {
        if(mailinglistId == 0) {
            return false;
        }
        
        Collection<Integer> adminForDisallowing = CollectionUtils.removeAll(adminService.getAdminsNamesMap(companyId).keySet(), allowedUserIds);
        boolean result = setAdminsDisallowedToUseMailinglist(companyId, mailinglistId, adminForDisallowing);
        if (result) {
            userActions.add(new UserAction("mailing list edit", "Allowed mailing list for admins: " + StringUtils.join(allowedUserIds, ", ")));
            userActions.add(new UserAction("mailing list edit", "Disallowed mailing list for admins: " + StringUtils.join(adminForDisallowing, ", ")));
        }
        return result;
    }

	@Override
	public List<Integer> getMailinglistsWithMailinglistApproval(int companyId) {
		return mailinglistApprovalDao.getMailinglistsWithMailinglistApproval(companyId);
	}
}
