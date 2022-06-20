/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;


import java.util.Date;
import java.util.Objects;

import com.agnitas.emm.core.workflow.beans.WorkflowDeadline;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;

public class WorkflowDeadlineImpl extends BaseWorkflowIcon implements WorkflowDeadline {
    
    public static int DEFAULT_AUTOIMPORT_DELAY_LIMIT = 1;

    private WorkflowDeadlineType deadlineType;
    private Date date;
    private WorkflowDeadlineTimeUnit timeUnit;
    private int delayValue;
    private int hour;
    private int minute;
    private boolean useTime;

    public WorkflowDeadlineImpl(){
        super();
        setType(WorkflowIconType.DEADLINE.getId());
    }

    @Override
    public WorkflowDeadlineType getDeadlineType() {
        return deadlineType;
    }

    @Override
    public void setDeadlineType(WorkflowDeadlineType deadlineType) {
        this.deadlineType = deadlineType;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public WorkflowDeadlineTimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public void setTimeUnit(WorkflowDeadlineTimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public int getDelayValue() {
        return delayValue;
    }

    @Override
    public void setDelayValue(int delayValue) {
        this.delayValue = delayValue;
    }

    @Override
    public int getHour() {
        return hour;
    }

    @Override
    public void setHour(int hour) {
        this.hour = hour;
    }

    @Override
    public int getMinute() {
        return minute;
    }

    @Override
    public void setMinute(int minute) {
        this.minute = minute;
    }

    @Override
    public boolean isUseTime() {
        return useTime;
    }

    @Override
    public void setUseTime(boolean useTime) {
        this.useTime = useTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkflowDeadlineImpl that = (WorkflowDeadlineImpl) o;
        return delayValue == that.delayValue &&
                hour == that.hour &&
                minute == that.minute &&
                useTime == that.useTime &&
                deadlineType == that.deadlineType &&
                Objects.equals(date, that.date) &&
                timeUnit == that.timeUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deadlineType, date, timeUnit, delayValue, hour, minute, useTime);
    }
}
