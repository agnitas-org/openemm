/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowImport;

public class WorkflowImportImpl extends BaseWorkflowIcon implements WorkflowImport {
    private int autoImportId;
    private boolean isErrorTolerant;

    public WorkflowImportImpl() {
        super();
        setType(WorkflowIconType.IMPORT.getId());
    }

    @Override
    public int getImportexportId() {
        return autoImportId;
    }

    @Override
    public void setImportexportId(int autoImportId) {
        this.autoImportId = autoImportId;
    }

    @Override
    public boolean isErrorTolerant() {
        return isErrorTolerant;
    }

    @Override
    public void setErrorTolerant(boolean isErrorTolerant) {
        this.isErrorTolerant = isErrorTolerant;
    }

    @Override
    public List<WorkflowDependency> getDependencies() {
        List<WorkflowDependency> dependencies = super.getDependencies();

        if (isFilled() && autoImportId > 0) {
            dependencies.add(WorkflowDependencyType.AUTO_IMPORT.forId(autoImportId));
        }

        return dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkflowImportImpl that = (WorkflowImportImpl) o;
        return autoImportId == that.autoImportId &&
                isErrorTolerant == that.isErrorTolerant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), autoImportId, isErrorTolerant);
    }
}
