/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.workflow;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflows")
public class WorkflowV2Controller {

    private final WorkflowRestService workflowService;

    public WorkflowV2Controller(WorkflowRestService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    @PermissionAllowed("workflow.show")
    public PageResponse<WorkflowLightResponse> list(Pageable pageable, Admin admin) {
        return workflowService.find(pageable, admin);
    }

    @PostMapping("/{workflowId:\\d+}/activate")
    @PermissionAllowed("workflow.activate")
    public WorkflowStatusResponse activate(@PathVariable @Positive int workflowId, Admin admin) {
        return workflowService.activate(workflowId, admin.getCompanyID());
    }

    @PostMapping("/{workflowId:\\d+}/deactivate")
    @PermissionAllowed("workflow.activate")
    public WorkflowStatusResponse deactivate(@PathVariable @Positive int workflowId, Admin admin) {
        return workflowService.deactivate(workflowId, admin.getCompanyID());
    }

    @PostMapping("/{workflowId:\\d+}/pause")
    @PermissionAllowed("workflow.activate")
    public WorkflowPauseResponse pause(@PathVariable @Positive int workflowId, Admin admin) {
        return workflowService.pause(workflowId, admin.getCompanyID());
    }
}
