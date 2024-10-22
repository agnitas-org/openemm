/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service.jobs;

import java.util.List;
import java.util.Locale;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.ComWorkflowActivationService;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.JobWorker;
import org.agnitas.service.UserActivityLogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorkflowStateTransitionJobWorker extends JobWorker {

    private static final Logger logger = LogManager.getLogger(WorkflowStateTransitionJobWorker.class);

    @Override
   	public String runJob() throws Exception {
   		ComWorkflowService workflowService = serviceLookupFactory.getBeanWorkflowService();

        List<Workflow> workflowsToDeactivate = workflowService.getWorkflowsToDeactivate(getCompaniesConstraints());

   		for (Workflow workflow : workflowsToDeactivate) {
   			int workflowId = workflow.getWorkflowId();
   			int companyId = workflow.getCompanyId();

   			switch (workflow.getStatus()) {
   				case STATUS_ACTIVE:
   					workflowService.changeWorkflowStatus(workflowId, companyId, Workflow.WorkflowStatus.STATUS_COMPLETE);
   					break;

   				case STATUS_TESTING:
   					workflowService.changeWorkflowStatus(workflowId, companyId, Workflow.WorkflowStatus.STATUS_TESTED);
   					break;

   				default:
   					break;
   			}
   		}
        processPausedWorkflows(workflowService);
        return null;
   	}

    private void processPausedWorkflows(ComWorkflowService workflowService) throws Exception {
        ComWorkflowActivationService activationService = serviceLookupFactory.getBeanWorkflowActivationService();
        UserActivityLogService userActivityLogService = serviceLookupFactory.getBeanUserActivityLogService();
        List<Workflow> workflowsToUnpause = workflowService.getWorkflowsToUnpause(getCompaniesConstraints());

        for (Workflow workflow : workflowsToUnpause) {
            Admin pauseAdmin = workflowService.getPauseAdmin(workflow.getWorkflowId(), workflow.getCompanyId());
            ServiceResult<List<UserAction>> result = activationService.autoUnpauseWorkflow(workflow, pauseAdmin);
            logUnpauseResult(userActivityLogService, workflow, pauseAdmin, result);
        }
    }

    private void logUnpauseResult(UserActivityLogService ualService, Workflow workflow, Admin admin,
                                  ServiceResult<List<UserAction>> result) {
        if (result.isSuccess()) {
            ualService.writeUserActivityLog(admin, 
                    "do unpause campaign (automatic)",
                    WorkflowUtils.getWorkflowDescription(workflow), logger);
            result.getResult().forEach(ua -> ualService.writeUserActivityLog(admin, ua, logger));
            result.getWarningMessages().forEach(error -> logWarn(error, admin.getLocale()));
        } else {
            logger.error("Can't reactivate campaign after pause expiration.");
            result.getErrorMessages().forEach(error -> logError(error, admin.getLocale()));
        }
    }

    private void logError(Message error, Locale locale) {
        logger.error(I18nString.getLocaleString(error.getCode(), locale, error.getArguments()));
    }
    
    private void logWarn(Message warn, Locale locale) {
        logger.warn(I18nString.getLocaleString(warn.getCode(), locale, warn.getArguments()));
    }
}
