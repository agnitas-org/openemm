/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.bean;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.beans.DependentEntityTypeEnum;

public enum ProfileFieldDependentType implements DependentEntityTypeEnum<ProfileFieldDependentType> {
    WORKFLOW(1),
    TARGET_GROUP(2);

    private final int id;

    ProfileFieldDependentType(int id) {
        this.id = id;
    }

    public static ProfileFieldDependentType fromId(int id) {
        return IntEnum.fromId(ProfileFieldDependentType.class, id);
    }

    public static ProfileFieldDependentType fromId(int id, boolean safe) {
        return IntEnum.fromId(ProfileFieldDependentType.class, id, safe);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Dependent<ProfileFieldDependentType> forId(int entityId, String entityShortname) {
        return new Dependent<>(this, entityId, entityShortname);
    }
}
