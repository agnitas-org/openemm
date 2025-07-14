/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.workflow;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.WorkflowActivationService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.ResumeCampaignRequest;
import com.agnitas.emm.springws.jaxb.ResumeCampaignResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.ArrayList;

@Endpoint
public class ResumeCampaignEndpoint extends BaseEndpoint {

    private final SecurityContextAccess securityContextAccess;
    private final WorkflowActivationService workflowActivationService;
    private final WorkflowService workflowService;
    private final AdminService adminService;

    public ResumeCampaignEndpoint(SecurityContextAccess securityContextAccess, WorkflowActivationService workflowActivationService, WorkflowService workflowService, AdminService adminService) {
        this.securityContextAccess = securityContextAccess;
        this.workflowActivationService = workflowActivationService;
        this.workflowService = workflowService;
        this.adminService = adminService;
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "ResumeCampaignRequest")
    public @ResponsePayload ResumeCampaignResponse pause(@RequestPayload ResumeCampaignRequest request) throws Exception {
        int workflowId = request.getCampaignID();
        int companyId = securityContextAccess.getWebserviceUserCompanyId();

        Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
        if (workflow == null) {
            throw new IllegalStateException("Workflow not found!");
        }

        if (workflow.getStatus().equals(Workflow.WorkflowStatus.STATUS_ACTIVE)) {
            return new ResumeCampaignResponse();
        }

        if (!workflow.getStatus().equals(Workflow.WorkflowStatus.STATUS_PAUSED)) {
            throw new IllegalStateException("Workflow cannot be activated because it is not paused!");
        }

        Admin admin = adminService.getAdmin(workflowService.getWorkflowSenderId(workflow), companyId);
        workflowService.deleteWorkflowTargetConditions(admin.getCompanyID(), workflowId);
        if (!workflowActivationService.activateWorkflow(workflowId, admin, false, true, false, new ArrayList<>(), new ArrayList<>(), new ArrayList<>())) {
            throw new RuntimeException("Workflow activation failed!");
        }

        workflowService.changeWorkflowStatus(workflowId, admin.getCompanyID(), Workflow.WorkflowStatus.STATUS_ACTIVE);
        return new ResumeCampaignResponse();
    }
}
