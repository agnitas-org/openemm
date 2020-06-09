/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.beans;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.beans.DependentEntityTypeEnum;

public enum TargetGroupDependentType implements DependentEntityTypeEnum<TargetGroupDependentType> {
    MAILING(1),
    MAILING_CONTENT(2),
    EXPORT_PROFILE(3),
    REPORT(4);

    private final int id;

    public static TargetGroupDependentType fromId(int id) {
        return IntEnum.fromId(TargetGroupDependentType.class, id);
    }

    public static TargetGroupDependentType fromId(int id, boolean safe) {
        return IntEnum.fromId(TargetGroupDependentType.class, id, safe);
    }

    TargetGroupDependentType(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Dependent<TargetGroupDependentType> forId(int entityId, String entityShortname) {
        return new Dependent<>(this, entityId, entityShortname);
    }
}
