/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.user.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.user.dto.CreateUiUserRequest;
import com.agnitas.emm.mcp.v1.user.dto.UiUserLightResponse;
import com.agnitas.emm.mcp.v1.user.dto.UiUserResponse;
import com.agnitas.emm.mcp.v1.user.dto.PatchUiUserRequest;

public interface UiUserMcpService {

    PageResponse<UiUserLightResponse> list(McpPageable pageable, Admin admin);

    UiUserResponse get(Integer userId, String username, int companyId);

    UiUserResponse create(CreateUiUserRequest request, Admin admin);

    UiUserResponse patch(Integer userId, String username, PatchUiUserRequest request, Admin admin);

    void delete(Integer userId, String username, int companyId);

}
