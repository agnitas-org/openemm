/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.target.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.Target;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.core.target.dto.PatchTargetCommand;
import com.agnitas.emm.mcp.v1.target.dto.CreateTargetGroupRequest;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupLightResponse;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupResponse;
import com.agnitas.emm.mcp.v1.target.dto.PatchTargetGroupRequest;
import com.agnitas.emm.mcp.v1.target.exception.TargetGroupDeletionException;
import com.agnitas.emm.mcp.v1.target.mapper.TargetGroupMcpMapper;
import com.agnitas.service.SimpleServiceResult;
import org.springframework.stereotype.Service;

@Service
public class TargetGroupMcpServiceImpl implements TargetGroupMcpService {

    private final TargetService targetService;
    private final TargetGroupMcpMapper mapper;

    public TargetGroupMcpServiceImpl(TargetService targetService, TargetGroupMcpMapper mapper) {
        this.targetService = targetService;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<TargetGroupLightResponse> list(McpPageable pageable, Admin admin) {
        TargetLightsOptions opts = TargetLightsOptions.builder()
                .setAdminId(admin.getAdminID())
                .setCompanyId(admin.getCompanyID())
                .setPageNumber(pageable.page())
                .setPageSize(pageable.size())
                .setCreationDate(new DateRange())
                .setChangeDate(new DateRange())
                .setDirection(Boolean.TRUE.equals(pageable.ascending()) ? "ASC" : "DESC")
                .setSorting(pageable.sortBy())
                .build();

        PaginatedList<TargetLight> targets = targetService.getTargetLightsPaginated(opts, null);
        return mapper.toPageResponse(targets);
    }

    @Override
    public TargetGroupResponse getById(int id, int companyId) {
        Target target = targetService.getTargetGroup(id, companyId);
        return mapper.toResponse(target);
    }

    @Override
    public TargetGroupResponse create(CreateTargetGroupRequest request, int companyId) {
        Target target = targetService.create(mapper.toCommand(request, companyId));
        return getById(target.getId(), companyId);
    }

    @Override
    public TargetGroupResponse patch(int id, PatchTargetGroupRequest request, int companyId) {
        PatchTargetCommand command = mapper.toCommand(request, id, companyId);
        targetService.patch(command);
        return getById(id, companyId);
    }

    @Override
    public void delete(int id, Admin admin) {
        SimpleServiceResult result = targetService.deleteTargetGroup(id, admin);
        if (!result.isSuccess()) {
            throw new TargetGroupDeletionException(result.getErrorMessages());
        }
    }

}
