/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import java.util.Date;

import com.agnitas.beans.IntEnum;

public interface WorkflowDeadline extends WorkflowIcon {

    boolean isUseTime();

    WorkflowDeadlineType getDeadlineType();

    void setDeadlineType(WorkflowDeadlineType deadlineType);

    Date getDate();

    void setDate(Date date);

    WorkflowDeadlineTimeUnit getTimeUnit();

    void setTimeUnit(WorkflowDeadlineTimeUnit timeUnit);

    int getDelayValue();

    void setDelayValue(int delayValue);

    int getHour();

    void setHour(int hour);

    int getMinute();

    void setMinute(int minute);

    void setUseTime(boolean useTime);

    enum WorkflowDeadlineType implements IntEnum {
        TYPE_DELAY(1),
        TYPE_FIXED_DEADLINE(2);

        private final int id;

        public static WorkflowDeadlineType fromId(int id) {
            return IntEnum.fromId(WorkflowDeadlineType.class, id);
        }

        public static WorkflowDeadlineType fromId(int id, boolean safe) {
            return IntEnum.fromId(WorkflowDeadlineType.class, id, safe);
        }

        WorkflowDeadlineType(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }

    enum WorkflowDeadlineTimeUnit implements IntEnum {
        TIME_UNIT_MINUTE(1),
        TIME_UNIT_HOUR(2),
        TIME_UNIT_DAY(3),
        TIME_UNIT_WEEK(4),
        TIME_UNIT_MONTH(5);

        private final int id;

        public static WorkflowDeadlineTimeUnit fromId(int id) {
            return IntEnum.fromId(WorkflowDeadlineTimeUnit.class, id);
        }

        public static WorkflowDeadlineTimeUnit fromId(int id, boolean safe) {
            return IntEnum.fromId(WorkflowDeadlineTimeUnit.class, id, safe);
        }

        WorkflowDeadlineTimeUnit(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
