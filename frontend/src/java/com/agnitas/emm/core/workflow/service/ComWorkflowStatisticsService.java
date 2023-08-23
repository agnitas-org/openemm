/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowStatisticDto;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

public class ComWorkflowStatisticsService {

	private ComWorkflowService workflowService;
    private ComOptimizationService optimizationService;
	private BirtStatisticsService birtStatisticsService;

	public String getReportUrl(int workflowId, Admin admin) throws Exception {
		WorkflowStatisticDto statistic = new WorkflowStatisticDto();

		statistic.setCompanyId(admin.getCompanyID());
		statistic.setFormat("html");
		statistic.setWorkflowId(workflowId);

		return birtStatisticsService.getWorkflowStatisticUrl(admin, statistic);
	}

    public int getFinalMailingID(int workflowId, int companyId){
		if (isTotalStatisticAvailable(workflowId, companyId)) {
			return optimizationService.getFinalMailingId(companyId, workflowId);
		}

        return 0;
    }
    
	public boolean isTotalStatisticAvailable(int workflowId, int companyId) {
		Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
		
		return workflow != null &&
				isTotalStatisticAvailable(workflow.getStatus(), workflow.getWorkflowIcons());
    }
    
    public boolean isTotalStatisticAvailable(Workflow.WorkflowStatus status, List<WorkflowIcon> icons) {
        return status == Workflow.WorkflowStatus.STATUS_COMPLETE &&
                WorkflowUtils.isAutoOptWorkflow(icons);
    }

	@Required
	public void setWorkflowService(ComWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@Required
    public void setOptimizationService(ComOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @Required
	public void setBirtStatisticsService(BirtStatisticsService birtStatisticsService) {
		this.birtStatisticsService = birtStatisticsService;
	}
}
