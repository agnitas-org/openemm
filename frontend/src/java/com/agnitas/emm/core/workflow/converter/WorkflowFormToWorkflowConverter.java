/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.workflow.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.web.forms.WorkflowForm;

@Component
public class WorkflowFormToWorkflowConverter implements Converter<WorkflowForm, Workflow> {

    @Override
    public Workflow convert(WorkflowForm workflowForm) {
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(workflowForm.getWorkflowId());
        workflow.setCompanyId(workflow.getCompanyId());
        workflow.setShortname(workflowForm.getShortname());
        workflow.setDescription(workflowForm.getDescription());
        workflow.setStatus(Workflow.WorkflowStatus.valueOf(workflowForm.getStatus().name()));
        workflow.setEditorPositionTop(workflowForm.getEditorPositionTop());
        workflow.setEditorPositionLeft(workflowForm.getEditorPositionLeft());
        workflow.setInner(workflowForm.isInner());
        workflow.setGeneralStartDate(workflowForm.getGeneralStartDate());
        workflow.setGeneralEndDate(workflowForm.getGeneralEndDate());
        workflow.setEndType(workflowForm.getEndType());
        workflow.setGeneralStartReaction(workflowForm.getGeneralStartReaction());
        workflow.setGeneralStartEvent(workflowForm.getGeneralStartEvent());
        workflow.setWorkflowIcons(workflowForm.getWorkflowIcons());
        workflow.setWorkflowSchema(workflowForm.getWorkflowSchema());

        return workflow;
    }
}
