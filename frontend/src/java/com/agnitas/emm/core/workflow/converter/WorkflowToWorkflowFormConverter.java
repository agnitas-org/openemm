/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.workflow.converter;

import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.web.forms.WorkflowForm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_ACTIVE;
import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_INACTIVE;
import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_NONE;

@Component
public class WorkflowToWorkflowFormConverter implements Converter<Workflow, WorkflowForm> {
    @Override
    public WorkflowForm convert(Workflow workflow) {
        WorkflowForm workflowForm = new WorkflowForm();
        workflowForm.setWorkflowId(workflow.getWorkflowId());
        workflowForm.setCompanyId(workflow.getCompanyId());
        workflowForm.setShortname(workflow.getShortname());
        workflowForm.setDescription(workflow.getDescription());
        workflowForm.setStatus(WorkflowForm.WorkflowStatus.valueOf(workflow.getStatus().name()));
        workflowForm.setStatusMaybeChangedTo(calculateNewStatus(workflowForm.getStatus()));
        workflowForm.setEditorPositionTop(workflow.getEditorPositionTop());
        workflowForm.setEditorPositionLeft(workflow.getEditorPositionLeft());
        workflowForm.setInner(workflow.isInner());
        workflowForm.setGeneralStartDate(workflow.getGeneralStartDate());
        workflowForm.setGeneralEndDate(workflow.getGeneralEndDate());
        workflowForm.setEndType(workflow.getEndType());
        workflowForm.setGeneralStartReaction(workflow.getGeneralStartReaction());
        workflowForm.setGeneralStartEvent(workflow.getGeneralStartEvent());
        workflowForm.setWorkflowIcons(workflow.getWorkflowIcons());
        workflowForm.setWorkflowSchema(workflow.getWorkflowSchema());

        return workflowForm;
    }

    private WorkflowForm.WorkflowStatus calculateNewStatus(WorkflowForm.WorkflowStatus status) {
        WorkflowForm.WorkflowStatus result;
        switch (status) {
            case STATUS_ACTIVE:
                result = STATUS_INACTIVE;
                break;

            case STATUS_OPEN:
            case STATUS_INACTIVE:
            case STATUS_TESTED:
                result = STATUS_ACTIVE;
                break;

            default:
                result = STATUS_NONE;
                break;
        }

        return result;
    }
}
