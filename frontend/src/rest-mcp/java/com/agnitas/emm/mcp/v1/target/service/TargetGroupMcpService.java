/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.target.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.target.dto.CreateTargetGroupRequest;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupLightResponse;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupResponse;
import com.agnitas.emm.mcp.v1.target.dto.PatchTargetGroupRequest;

public interface TargetGroupMcpService {

    PageResponse<TargetGroupLightResponse> list(McpPageable pageable, Admin admin);

    TargetGroupResponse getById(int id, int companyId);

    TargetGroupResponse create(CreateTargetGroupRequest request, int companyId);

    TargetGroupResponse patch(int id, PatchTargetGroupRequest request, int companyId);

    void delete(int id, Admin admin);

}
