/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.target.service;

import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.Target;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.target.dto.PatchTargetCommand;
import com.agnitas.emm.core.target.dto.TargetInclude;
import com.agnitas.emm.core.target.dto.TargetView;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.target.dto.CreateTargetGroupRequest;
import com.agnitas.emm.mcp.v1.target.dto.PatchTargetGroupRequest;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupLightResponse;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupResponse;
import com.agnitas.emm.mcp.v1.target.exception.TargetGroupDeletionException;
import com.agnitas.emm.mcp.v1.target.mapper.TargetGroupMcpMapper;
import com.agnitas.service.SimpleServiceResult;
import org.apache.commons.collections4.CollectionUtils;
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
    public PageResponse<TargetGroupLightResponse> list(McpPageable pageable, Set<TargetInclude> include, Admin admin) {
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
        PaginatedList<TargetView> targets = targetService.find(opts, include, admin);
        return mapper.toPageResponse(targets, mapper::toLightResponse);
    }

    private TargetGroupResponse getById(int id, Admin admin) {
        return getById(id, Set.of(), admin);
    }

    @Override
    public TargetGroupResponse getById(int targetId, Set<TargetInclude> include, Admin admin) {
        TargetView target = targetService.getTargetGroup(targetId, admin, include);
        return mapper.toResponse(target);
    }

    @Override
    public TargetGroupResponse create(CreateTargetGroupRequest request, Admin admin) {
        Target target = targetService.create(mapper.toCommand(request, admin.getCompanyID()));
        return getById(target.getId(), admin);
    }

    @Override
    public TargetGroupResponse patch(int id, PatchTargetGroupRequest request, Admin admin) {
        PatchTargetCommand command = mapper.toCommand(request, id, admin.getCompanyID());
        targetService.patch(command);
        return getById(id, admin);
    }

    @Override
    public void delete(int id, Admin admin) {
        SimpleServiceResult result = targetService.deleteTargetGroup(id, admin);
        if (!result.isSuccess()) {
            throw new TargetGroupDeletionException(result.getErrorMessages());
        }
    }

    @Override
    public List<TargetGroupResponse> getMailingTargets(Mailing mailing, Admin admin) {
        return CollectionUtils.emptyIfNull(mailing.getTargetGroups()).stream()
                .map(id -> getById(id, admin))
                .toList();
    }
}
