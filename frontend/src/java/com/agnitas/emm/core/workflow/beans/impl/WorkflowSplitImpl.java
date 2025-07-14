/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.Objects;

import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowSplit;

public class WorkflowSplitImpl extends BaseWorkflowIcon implements WorkflowSplit {

    private String splitType;

    public WorkflowSplitImpl() {
        super();
        setType(WorkflowIconType.SPLIT.getId());
    }

    public String getsplitType() {
        return splitType;
    }

    public void setsplitType(String splitType) {
        this.splitType = splitType;
    }

    @Override
    public boolean equalsIgnoreI18n(Object o) {
        WorkflowSplitImpl that = (WorkflowSplitImpl) o;
        return super.equalsIgnoreI18n(o)
            && Objects.equals(splitType, that.splitType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), splitType);
    }
}
