/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.form;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.ArrayUtils;

import com.agnitas.emm.core.target.beans.TargetGroupDependentType;

// TODO: EMMGUI-714: remove when old design will be removed and replace with simple pagination form
public class TargetDependentsListForm extends PaginationForm {

    private int targetId;
    private String[] filterTypes;

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String[] getFilterTypes() {
        return filterTypes;
    }

    public Set<TargetGroupDependentType> getFilterTypesSet() {
        if (ArrayUtils.isEmpty(filterTypes)) {
            return Collections.emptySet();
        } else {
            return Arrays.stream(filterTypes).map(TargetGroupDependentType::valueOf).collect(Collectors.toSet());
        }
    }

    public void setFilterTypes(String[] filterTypes) {
        this.filterTypes = filterTypes;
    }
}
