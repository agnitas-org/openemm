/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowStop;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

//Start/Stop icons uses the same modals and stored in the same table.
//Furthermore it is possible to know know which type of icon we have (if icon is not filled we don't know is it start or end)
//So sometimes startType could arrive in requests/responses for the Stop icon. This is simple fix for this.
@JsonIgnoreProperties(value = { "startType" })
public class WorkflowStopImpl extends WorkflowStartStopImpl implements WorkflowStop {

    private WorkflowEndType endType;

    public WorkflowStopImpl() {
        super();
        setType(WorkflowIconType.STOP.getId());
    }

    @Override
    public WorkflowEndType getEndType() {
        return endType;
    }

    @Override
    public void setEndType(WorkflowEndType endType) {
        this.endType = endType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkflowStopImpl that = (WorkflowStopImpl) o;
        return endType == that.endType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endType);
    }
}
