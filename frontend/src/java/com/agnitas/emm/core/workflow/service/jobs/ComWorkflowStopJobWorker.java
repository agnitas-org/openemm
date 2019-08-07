/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service.jobs;

import java.util.List;

import org.agnitas.service.JobWorker;

import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;

/**
 * Handles campaign stop
 */
public class ComWorkflowStopJobWorker extends JobWorker {

	@Override
	public void runJob() throws Exception {
		ComWorkflowService workflowService = serviceLookupFactory.getBeanWorkflowService();
		List<Workflow> workflowsToDeactivate = workflowService.getWorkflowsToDeactivate(getCompaniesConstrains());

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
	}

}
