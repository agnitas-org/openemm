/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import com.agnitas.beans.IntEnum;

/**
 * Used for workflow dependencies representation in database.
 * So make sure that ids are unique and keep in mind to never change them once assigned.
 * Also make sure to keep in sync {@link org.agnitas.util.Const.WorkflowDependencyType}.
 */
public enum WorkflowDependencyType implements IntEnum {
    ARCHIVE(1),
    AUTO_EXPORT(2),
    AUTO_IMPORT(3),
    MAILING_DELIVERY(4),
    MAILING_LINK(5),
    MAILING_REFERENCE(6),
    MAILINGLIST(7),
    PROFILE_FIELD(8),
    PROFILE_FIELD_HISTORY(9),
    REPORT(10),
    TARGET_GROUP(11),
    TARGET_GROUP_CONDITION(13),
    USER_FORM(12);

    private final int id;

    public static WorkflowDependencyType fromId(int id) {
        return IntEnum.fromId(WorkflowDependencyType.class, id);
    }

    public static WorkflowDependencyType fromId(int id, boolean safe) {
        return IntEnum.fromId(WorkflowDependencyType.class, id, safe);
    }

    WorkflowDependencyType(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public WorkflowDependency forId(int entityId) {
        return WorkflowDependency.from(this, entityId);
    }

    public WorkflowDependency forName(String entityName) {
        return WorkflowDependency.from(this, entityName);
    }
}
