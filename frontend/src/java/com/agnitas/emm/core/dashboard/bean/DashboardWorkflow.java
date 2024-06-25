/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dashboard.bean;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.WorkflowStop;

import java.util.Date;
import java.util.List;

public class DashboardWorkflow {

    public enum Status implements IntEnum {
        ACTIVE(1, List.of(Workflow.WorkflowStatus.STATUS_ACTIVE)),
        COMPLETE(2, List.of(Workflow.WorkflowStatus.STATUS_INACTIVE, Workflow.WorkflowStatus.STATUS_COMPLETE));

        private final int id;
        private List<Workflow.WorkflowStatus> realStatuses;

        Status(int id, List<Workflow.WorkflowStatus> realStatuses) {
            this.id = id;
            this.realStatuses = realStatuses;

        }

        @Override
        public int getId() {
            return id;
        }

        public static Status fromId(int id, boolean safe) {
            return IntEnum.fromId(Status.class, id, safe);
        }

        public List<Workflow.WorkflowStatus> getRealStatuses() {
            return realStatuses;
        }
    }

    private int id;
    private String name;
    private Date startDate;
    private Date endDate;
    private Status status;
    private WorkflowStop.WorkflowEndType endType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public WorkflowStop.WorkflowEndType getEndType() {
        return endType;
    }

    public void setEndType(WorkflowStop.WorkflowEndType endType) {
        this.endType = endType;
    }
}
