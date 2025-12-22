/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dashboard.bean;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.WorkflowStop;

public class DashboardWorkflow {

    public enum Status implements IntEnum {
        ACTIVE(1, List.of(Workflow.WorkflowStatus.STATUS_ACTIVE)),
        COMPLETE(2, List.of(Workflow.WorkflowStatus.STATUS_INACTIVE, Workflow.WorkflowStatus.STATUS_COMPLETE));

        private final int id;
        private final List<Workflow.WorkflowStatus> realStatuses;

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

    private final int id;
    private final String name;
    private final Date startDate;
    private final Date endDate;
    private final Status status;
    private final WorkflowStop.WorkflowEndType endType;

    public DashboardWorkflow(int id, String name, Date startDate, Date endDate, Status status, WorkflowStop.WorkflowEndType endType) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.endType = endType;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Status getStatus() {
        return status;
    }

    public WorkflowStop.WorkflowEndType getEndType() {
        return endType;
    }

}
