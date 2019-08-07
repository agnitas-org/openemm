/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import java.util.List;

import com.agnitas.beans.IntEnum;

public interface WorkflowRecipient extends WorkflowIcon {

    int getMailinglistId();

	void setMailinglistId(int mailinglistId);

	List<Integer> getTargets();

	void setTargets(List<Integer> targets);

    WorkflowTargetOption getTargetsOption();

    void setTargetsOption(WorkflowTargetOption targetsOption);

    enum WorkflowTargetOption implements IntEnum {
        ALL_TARGETS_REQUIRED(1),
        NOT_IN_TARGETS(2),
        ONE_TARGET_REQUIRED(3);

        private final int id;

        public static WorkflowTargetOption fromId(int id) {
            return IntEnum.fromId(WorkflowTargetOption.class, id);
        }

        public static WorkflowTargetOption fromId(int id, boolean safe) {
            return IntEnum.fromId(WorkflowTargetOption.class, id, safe);
        }

        WorkflowTargetOption(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
