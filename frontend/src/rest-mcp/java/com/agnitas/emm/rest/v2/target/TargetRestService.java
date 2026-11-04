/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.target;

import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.Target;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.target.dto.CreateTargetCommand;
import com.agnitas.emm.core.target.dto.PatchTargetCommand;
import com.agnitas.emm.core.target.dto.TargetInclude;
import com.agnitas.emm.core.target.dto.TargetView;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.rest.v2.infrastructure.exception.HttpStatusException;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.service.SimpleServiceResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TargetRestService {

    private final TargetRestMapper mapper;
    private final TargetService targetService;

    TargetRestService(TargetRestMapper mapper, TargetService targetService) {
        this.mapper = mapper;
        this.targetService = targetService;
    }

    public PageResponse<TargetLightResponse> find(Pageable pageable, Set<TargetInclude> include, Admin admin) {
        TargetLightsOptions opts = TargetLightsOptions.builder()
                .setAdminId(admin.getAdminID())
                .setCompanyId(admin.getCompanyID())
                .setPageNumber(pageable.getPageNumber())
                .setPageSize(pageable.getPageSize())
                .setCreationDate(new DateRange())
                .setChangeDate(new DateRange())
                .setDirection(mapper.toDirection(pageable.getSort()) ? "ASC" : "DESC")
                .setSorting(mapper.toSortProperty(pageable.getSort()))
                .build();
        PaginatedList<TargetView> targets = targetService.find(opts, include, admin);
        return mapper.toPageResponse(targets, mapper::toLightResponse);
    }

    public List<TargetResponse> getMailingTargets(Mailing mailing, Admin admin) {
        return CollectionUtils.emptyIfNull(mailing.getTargetGroups()).stream()
                .map(id -> find(id, admin))
                .toList();
    }

    public TargetResponse find(int targetId, Admin admin) {
        return find(targetId, Set.of(), admin);
    }

    public TargetResponse find(int targetId, Set<TargetInclude> include, Admin admin) {
        TargetView target = targetService.getTargetGroup(targetId, admin, include);
        return mapper.toResponse(target);
    }

    public TargetResponse create(CreateTargetRequest request, Admin admin) {
        CreateTargetCommand command = mapper.toCommand(request, admin.getCompanyID());
        Target target = targetService.create(command);
        return find(target.getId(), admin);
    }

    public TargetResponse patch(int id, PatchTargetRequest request, Admin admin) {
        PatchTargetCommand command = mapper.toCommand(request, id, admin.getCompanyID());
        targetService.patch(command);
        return find(id, admin);
    }

    public void delete(int id, Admin admin) {
        SimpleServiceResult result = targetService.deleteTargetGroup(id, admin);
        if (!result.isSuccess()) {
            throw new HttpStatusException(HttpStatus.CONFLICT, List.copyOf(result.getErrorMessages()));
        }
    }
}
