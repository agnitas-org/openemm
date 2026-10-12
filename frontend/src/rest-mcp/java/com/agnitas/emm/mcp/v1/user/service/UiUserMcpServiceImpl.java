/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.user.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.admin.service.CreateUiUserCommand;
import com.agnitas.emm.core.admin.service.PatchUiUserCommand;
import com.agnitas.emm.core.admin.service.UserListOpts;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.user.dto.CreateUiUserRequest;
import com.agnitas.emm.mcp.v1.user.dto.PatchUiUserRequest;
import com.agnitas.emm.mcp.v1.user.dto.UiUserLightResponse;
import com.agnitas.emm.mcp.v1.user.dto.UiUserResponse;
import com.agnitas.emm.mcp.v1.user.mapper.UiUserMcpMapper;
import org.springframework.stereotype.Service;

@Service
public class UiUserMcpServiceImpl implements UiUserMcpService {

    private final AdminService adminService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final UiUserMcpMapper mapper;

    public UiUserMcpServiceImpl(
            AdminService adminService,
            MailinglistApprovalService mailinglistApprovalService,
            UiUserMcpMapper mapper
    ) {
        this.adminService = adminService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<UiUserLightResponse> list(McpPageable pageable, Admin admin) {
        UserListOpts opts = mapper.toListOpts(pageable, admin.getCompanyID(), admin.getLocale());
        PaginatedList<AdminEntry> list = adminService.getAdminList(opts);
        return mapper.toPageResponse(list);
    }

    @Override
    public UiUserResponse get(Integer userId, String username, int companyId) {
        requireExactlyOneIdentifier(userId, username);
        Admin uiUser = userId != null
                ? adminService.getUiUserById(userId, companyId)
                : adminService.getUiUser(username, companyId);

        return mapper.toResponse(uiUser, mailinglistApprovalService.getDisabledMailinglists(uiUser));
    }

    @Override
    public UiUserResponse create(CreateUiUserRequest request, Admin admin) {
        CreateUiUserCommand command = mapper.toCommand(request);
        Admin uiUser = adminService.createUiUser(command, admin);
        return get(uiUser.getAdminID(), null, admin.getCompanyID());
    }

    @Override
    public UiUserResponse patch(Integer userId, String username, PatchUiUserRequest request, Admin admin) {
        requireExactlyOneIdentifier(userId, username);
        PatchUiUserCommand command = mapper.toCommand(request);
        if (userId != null) {
            adminService.patchUiUser(userId, command, admin);
            return get(userId, null, admin.getCompanyID());
        }
        adminService.patchUiUser(username, command, admin);
        return get(null, username, admin.getCompanyID());
    }

    @Override
    public void delete(Integer userId, String username, int companyId) {
        requireExactlyOneIdentifier(userId, username);
        if (userId != null) {
            adminService.deleteUiAdmin(userId, companyId);
        } else {
            adminService.deleteUiAdmin(username, companyId);
        }
    }

    private void requireExactlyOneIdentifier(Integer userId, String username) {
        if ((userId == null) == (username == null)) {
            throw ValidationException.exact("Exactly one of userId or username must be provided");
        }
    }
}
