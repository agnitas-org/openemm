/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.IntEnum;

public class Workflow {

	private int workflowId;
	private int companyId;
	private String shortname;
	private String description;
	private WorkflowStatus status = WorkflowStatus.STATUS_OPEN;
    private int editorPositionTop;
    private int editorPositionLeft;
	private boolean inner;
    private Date generalStartDate;
    private Date generalEndDate;
    private WorkflowStop.WorkflowEndType endType;
    private WorkflowReactionType generalStartReaction;
    private WorkflowStart.WorkflowStartEventType generalStartEvent;
    private List<WorkflowIcon> workflowIcons;
    private String workflowSchema;

    public int getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public WorkflowStatus getStatus() {
		return status;
	}

	public void setStatus(WorkflowStatus status) {
		this.status = status;
	}

    public int getEditorPositionTop() {
        return editorPositionTop;
    }

    public void setEditorPositionTop(int editorPositionTop) {
        this.editorPositionTop = editorPositionTop;
    }

    public int getEditorPositionLeft() {
        return editorPositionLeft;
    }

    public void setEditorPositionLeft(int editorPositionLeft) {
        this.editorPositionLeft = editorPositionLeft;
    }

	public boolean isInner() {
		return inner;
	}

	public void setInner(boolean inner) {
		this.inner = inner;
	}

    public Date getGeneralStartDate() {
        return generalStartDate;
    }

    public void setGeneralStartDate(Date generalStartDate) {
        this.generalStartDate = generalStartDate;
    }

    public Date getGeneralEndDate() {
        return generalEndDate;
    }

    public void setGeneralEndDate(Date generalEndDate) {
        this.generalEndDate = generalEndDate;
    }

    public WorkflowReactionType getGeneralStartReaction() {
        return generalStartReaction;
    }

    public void setGeneralStartReaction(WorkflowReactionType generalStartReaction) {
        this.generalStartReaction = generalStartReaction;
    }

    public WorkflowStart.WorkflowStartEventType getGeneralStartEvent() {
        return generalStartEvent;
    }

    public void setGeneralStartEvent(WorkflowStart.WorkflowStartEventType generalStartEvent) {
        this.generalStartEvent = generalStartEvent;
    }

    public WorkflowStop.WorkflowEndType getEndType() {
        return endType;
    }

    public void setEndType(WorkflowStop.WorkflowEndType endType) {
        this.endType = endType;
    }

    public String getStatusString() {
        return status.toString();
    }

    public void setStatusString(String status) {
        if (StringUtils.isEmpty(status)) {
            this.status = null;
        }
        this.status = WorkflowStatus.valueOf(status);
    }

    public String getWorkflowSchema() {
        return workflowSchema;
    }

    public void setWorkflowSchema(String workflowSchema) {
        this.workflowSchema = workflowSchema;
    }

    public List<WorkflowIcon> getWorkflowIcons() {
        return workflowIcons;
    }

    public void setWorkflowIcons(List<WorkflowIcon> workflowIcons) {
        this.workflowIcons = workflowIcons;
    }

    public enum WorkflowStatus implements IntEnum {
        STATUS_NONE(0, true, "none"), // Just a special value, not a valid campaign's status
        STATUS_OPEN(1, true, "open"),
        STATUS_ACTIVE(2, false, "active"),
        STATUS_INACTIVE(3, true, "inactive"),
        STATUS_COMPLETE(4, false, "completed"),
        STATUS_TESTING(5, false, "testing"),
        STATUS_TESTED(6, true, "tested"),
        STATUS_FAILED(7, true, "failed"),
        STATUS_TESTING_FAILED(8, true, "testing_failed"),
        STATUS_PAUSED(9, true, "paused");
        
        private final int id;
        private final boolean changeable;
        private final String name;

        public static WorkflowStatus fromId(int id) {
            return IntEnum.fromId(WorkflowStatus.class, id);
        }

        public static WorkflowStatus fromId(int id, boolean safe) {
            return IntEnum.fromId(WorkflowStatus.class, id, safe);
        }

        WorkflowStatus(int id, boolean changeable, String name) {
            this.id = id;
            this.changeable = changeable;
            this.name = name;
        }

        @Override
        public int getId() {
            return id;
        }

        public boolean isChangeable() {
            return changeable;
        }
        
        public String getName() {
            return name;
        }
    }
}
