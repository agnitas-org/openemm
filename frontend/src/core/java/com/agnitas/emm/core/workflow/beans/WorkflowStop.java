/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;


import com.agnitas.beans.IntEnum;

public interface WorkflowStop extends WorkflowStartStop {

    WorkflowEndType getEndType();

    void setEndType(WorkflowEndType endType);

    enum WorkflowEndType implements IntEnum {
        AUTOMATIC(1),
        DATE(2);

        private final int id;

        public static WorkflowEndType fromId(int id) {
            return IntEnum.fromId(WorkflowEndType.class, id);
        }

        public static WorkflowEndType fromId(int id, boolean safe) {
            return IntEnum.fromId(WorkflowEndType.class, id, safe);
        }

        WorkflowEndType(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
