/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;


import com.agnitas.beans.IntEnum;

public interface WorkflowStart extends WorkflowStartStop {

    WorkflowStartType getStartType();

    void setStartType(WorkflowStartType startType);

    enum WorkflowStartType implements IntEnum {
        OPEN(1),
        DATE(2),
        EVENT(3);

        private final int id;

        public static WorkflowStartType fromId(int id) {
            return IntEnum.fromId(WorkflowStartType.class, id);
        }

        public static WorkflowStartType fromId(int id, boolean safe) {
            return IntEnum.fromId(WorkflowStartType.class, id, safe);
        }

        WorkflowStartType(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }

    enum WorkflowStartEventType implements IntEnum {
        EVENT_REACTION(1),
        EVENT_DATE(2);

        private final int id;

        public static WorkflowStartEventType fromId(int id) {
            return IntEnum.fromId(WorkflowStartEventType.class, id);
        }

        public static WorkflowStartEventType fromId(int id, boolean safe) {
            return IntEnum.fromId(WorkflowStartEventType.class, id, safe);
        }

        WorkflowStartEventType(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
