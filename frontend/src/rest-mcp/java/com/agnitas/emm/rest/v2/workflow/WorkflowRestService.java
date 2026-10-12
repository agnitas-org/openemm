/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.workflow;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.WorkflowActivationService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
class WorkflowRestService {

    private final WorkflowActivationService activationService;
    private final WorkflowService workflowService;
    private final ConfigService configService;
    private final WorkflowRestMapper mapper;

    WorkflowRestService(
            WorkflowActivationService activationService,
            WorkflowService workflowService,
            ConfigService configService,
            WorkflowRestMapper mapper
    ) {
        this.activationService = activationService;
        this.workflowService = workflowService;
        this.configService = configService;
        this.mapper = mapper;
    }

    public PageResponse<WorkflowLightResponse> find(Pageable pageable, Admin admin) {
        PaginatedList<Workflow> list = workflowService.paginatedList(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                admin
        );
        int companyId = admin.getCompanyID();
        int pauseExpirationHours = configService.getIntegerValue(ConfigValue.WorkflowPauseExpirationHours, companyId);
        return mapper.toPageResponse(list, workflow -> mapper.toLightResponse(
                workflow,
                workflowService.getPauseTime(workflow, companyId).orElse(null),
                workflow.getStatus() == Workflow.WorkflowStatus.STATUS_PAUSED ? pauseExpirationHours : 0
        ));
    }

    public WorkflowPauseResponse pause(int workflowId, int companyId) {
        Workflow.WorkflowStatus newStatus = activationService.pause(workflowId, companyId);
        return new WorkflowPauseResponse(
                workflowId,
                newStatus.getName(),
                workflowService.getPauseDate(workflowId, companyId).toInstant(),
                configService.getIntegerValue(ConfigValue.WorkflowPauseExpirationHours, companyId)
        );
    }

    public WorkflowStatusResponse activate(int workflowId, int companyId) {
        Workflow.WorkflowStatus newStatus = activationService.activate(workflowId, companyId);
        return new WorkflowStatusResponse(workflowId, newStatus.getName());
    }

    public WorkflowStatusResponse deactivate(int workflowId, int companyId) {
        Workflow.WorkflowStatus newStatus = activationService.deactivate(workflowId, companyId);
        return new WorkflowStatusResponse(workflowId, newStatus.getName());
    }
}
