/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.user;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.admin.service.CreateUiUserCommand;
import com.agnitas.emm.core.admin.service.PatchUiUserCommand;
import com.agnitas.emm.core.admin.service.UserListOpts;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
class UiUserRestService {

    private final UiUserRestMapper mapper;
    private final AdminService adminService;
    private final MailinglistApprovalService mailinglistApprovalService;

    public UiUserRestService(
            UiUserRestMapper mapper,
            AdminService adminService,
            MailinglistApprovalService mailinglistApprovalService
    ) {
        this.mapper = mapper;
        this.adminService = adminService;
        this.mailinglistApprovalService = mailinglistApprovalService;
    }

    PageResponse<UiUserLightResponse> list(Pageable pageable, Admin admin) {
        UserListOpts opts = mapper.toListOpts(pageable, admin.getCompanyID(), admin.getLocale());
        PaginatedList<AdminEntry> list = adminService.getAdminList(opts);
        return mapper.toPageResponse(list, mapper::toLightResponse);
    }

    UiUserResponse find(int id, int companyId) {
        Admin uiUser = adminService.getUiUserById(id, companyId);
        return mapper.toResponse(uiUser, mailinglistApprovalService.getDisabledMailinglists(uiUser));
    }

    UiUserResponse find(String username, int companyId) {
        Admin uiUser = adminService.getUiUser(username, companyId);
        return find(uiUser.getAdminID(), companyId);
    }

    void delete(int id, int companyId) {
        adminService.deleteUiAdmin(id, companyId);
    }

    void delete(String username, int companyId) {
        adminService.deleteUiAdmin(username, companyId);
    }

    public UiUserResponse create(CreateUiUserRequest request, Admin admin) {
        CreateUiUserCommand command = mapper.toCommand(request);
        Admin uiUser = adminService.createUiUser(command, admin);
        return find(uiUser.getAdminID(), admin.getCompanyID());
    }

    public UiUserResponse patch(int id, PatchUiUserRequest request, Admin admin) {
        PatchUiUserCommand command = mapper.toCommand(request);
        adminService.patchUiUser(id, command, admin);
        return find(id, admin.getCompanyID());
    }

    public UiUserResponse patch(String username, PatchUiUserRequest request, Admin admin) {
        PatchUiUserCommand command = mapper.toCommand(request);
        adminService.patchUiUser(username, command, admin);
        return find(username, admin.getCompanyID());
    }
}
