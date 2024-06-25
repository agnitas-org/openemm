/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.workflow;

import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.jaxb.PauseCampaignRequest;
import org.agnitas.emm.springws.jaxb.PauseCampaignResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class PauseCampaignEndpoint extends BaseEndpoint {

    private final SecurityContextAccess securityContextAccess;
    private final ComWorkflowService workflowService;

    public PauseCampaignEndpoint(SecurityContextAccess securityContextAccess, ComWorkflowService workflowService) {
        this.securityContextAccess = securityContextAccess;
        this.workflowService = workflowService;
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "PauseCampaignRequest")
    public @ResponsePayload PauseCampaignResponse pause(@RequestPayload PauseCampaignRequest request) throws Exception {
        int workflowId = request.getCampaignID();
        int companyId = securityContextAccess.getWebserviceUserCompanyId();

        Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
        if (workflow == null) {
            throw new IllegalStateException("Workflow not found!");
        }

        if (workflow.getStatus().equals(Workflow.WorkflowStatus.STATUS_PAUSED)) {
            return new PauseCampaignResponse();
        }

        if (!workflow.getStatus().equals(Workflow.WorkflowStatus.STATUS_ACTIVE)) {
            throw new IllegalStateException("Workflow is not active. Impossible to pause it!");
        }


        workflowService.savePausedSchemaForUndo(workflow);
        workflowService.changeWorkflowStatus(workflow.getWorkflowId(), workflow.getCompanyId(), Workflow.WorkflowStatus.STATUS_PAUSED);

        return new PauseCampaignResponse();
    }
}
