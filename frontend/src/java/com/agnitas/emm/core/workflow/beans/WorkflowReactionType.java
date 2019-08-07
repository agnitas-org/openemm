/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import com.agnitas.beans.IntEnum;

public enum WorkflowReactionType implements IntEnum {
    OPENED(1),
    NOT_OPENED(2),
    CLICKED(3),
    NOT_CLICKED(4),
    BOUGHT(5),
    NOT_BOUGHT(6),
    DOWNLOAD(7),
    CHANGE_OF_PROFILE(8),
    WAITING_FOR_CONFIRM(9),
    OPT_IN(10),
    OPT_OUT(11),
    CLICKED_LINK(12),
    OPENED_AND_CLICKED(13),
    OPENED_OR_CLICKED(14),
    CONFIRMED_OPT_IN(15);

    private final int id;

    WorkflowReactionType(int id) {
        this.id = id;
    }

    public static WorkflowReactionType fromId(int id) {
        return IntEnum.fromId(WorkflowReactionType.class, id);
    }

    public static WorkflowReactionType fromId(int id, boolean safe) {
        return IntEnum.fromId(WorkflowReactionType.class, id, safe);
    }

    @Override
    public int getId() {
        return id;
    }
    
    public static WorkflowReactionType fromName(String name) {
        try {
            return WorkflowReactionType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
