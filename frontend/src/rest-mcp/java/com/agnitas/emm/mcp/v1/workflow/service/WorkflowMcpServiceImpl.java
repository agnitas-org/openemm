/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.workflow.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.WorkflowActivationService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.workflow.WorkflowLightResponse;
import com.agnitas.emm.mcp.v1.workflow.WorkflowPauseResponse;
import com.agnitas.emm.mcp.v1.workflow.WorkflowStatusResponse;
import com.agnitas.emm.mcp.v1.workflow.mapper.WorkflowMcpMapper;
import org.springframework.stereotype.Service;

@Service
public class WorkflowMcpServiceImpl implements WorkflowMcpService {

    private final WorkflowActivationService activationService;
    private final WorkflowService workflowService;
    private final ConfigService configService;
    private final WorkflowMcpMapper mapper;

    public WorkflowMcpServiceImpl(
            WorkflowActivationService activationService,
            WorkflowService workflowService,
            ConfigService configService,
            WorkflowMcpMapper mapper
    ) {
        this.activationService = activationService;
        this.workflowService = workflowService;
        this.configService = configService;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<WorkflowLightResponse> list(McpPageable pageable, Admin admin) {
        int companyId = admin.getCompanyID();
        int pauseExpirationHours = configService.getIntegerValue(ConfigValue.WorkflowPauseExpirationHours, companyId);
        PaginatedList<Workflow> list = workflowService.paginatedList(pageable.page(), pageable.size(), admin);

        return mapper.toPageResponse(list, workflow ->
                mapper.toLightResponse(
                        workflow,
                        workflowService.getPauseTime(workflow, companyId).orElse(null),
                        workflow.getStatus() == Workflow.WorkflowStatus.STATUS_PAUSED ? pauseExpirationHours : 0
                )
        );
    }

    @Override
    public WorkflowStatusResponse activate(int workflowId, int companyId) {
        Workflow.WorkflowStatus newStatus = activationService.activate(workflowId, companyId);
        return new WorkflowStatusResponse(workflowId, newStatus.getName());
    }

    @Override
    public WorkflowStatusResponse deactivate(int workflowId, int companyId) {
        Workflow.WorkflowStatus newStatus = activationService.deactivate(workflowId, companyId);
        return new WorkflowStatusResponse(workflowId, newStatus.getName());
    }

    @Override
    public WorkflowPauseResponse pause(int workflowId, int companyId) {
        Workflow.WorkflowStatus newStatus = activationService.pause(workflowId, companyId);
        return new WorkflowPauseResponse(
                workflowId,
                newStatus.getName(),
                workflowService.getPauseDate(workflowId, companyId).toInstant(),
                configService.getIntegerValue(ConfigValue.WorkflowPauseExpirationHours, companyId)
        );
    }

}
