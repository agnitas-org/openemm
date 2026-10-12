/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.target;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.Target;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.target.dto.CreateTargetCommand;
import com.agnitas.emm.core.target.dto.PatchTargetCommand;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.rest.v2.infrastructure.exception.HttpStatusException;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.service.SimpleServiceResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
class TargetRestService {

    private final TargetRestMapper mapper;
    private final TargetService targetService;

    TargetRestService(TargetRestMapper mapper, TargetService targetService) {
        this.mapper = mapper;
        this.targetService = targetService;
    }

    public PageResponse<TargetLightResponse> find(Pageable pageable, Admin admin) {
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
        PaginatedList<TargetLight> targets = targetService.getTargetLightsPaginated(opts, null);
        return mapper.toPageResponse(targets, mapper::toLightResponse);
    }

    public TargetResponse find(int targetId, int companyId) {
        Target target = targetService.getTargetGroup(targetId, companyId);
        return mapper.toResponse(target);
    }

    public TargetResponse create(CreateTargetRequest request, int companyId) {
        CreateTargetCommand command = mapper.toCommand(request, companyId);
        Target target = targetService.create(command);
        return find(target.getId(), companyId);
    }

    public TargetResponse patch(int id, PatchTargetRequest request, int companyId) {
        PatchTargetCommand command = mapper.toCommand(request, id, companyId);
        targetService.patch(command);
        return find(id, companyId);
    }

    public void delete(int id, Admin admin) {
        SimpleServiceResult result = targetService.deleteTargetGroup(id, admin);
        if (!result.isSuccess()) {
            throw new HttpStatusException(HttpStatus.CONFLICT, List.copyOf(result.getErrorMessages()));
        }
    }
}
