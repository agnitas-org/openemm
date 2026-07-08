/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowExport;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;

public class WorkflowExportImpl extends BaseWorkflowIcon implements WorkflowExport {
    private int autoExportId;

    public WorkflowExportImpl() {
        super();
        setType(WorkflowIconType.EXPORT.getId());
    }

    @Override
    public int getImportexportId() {
        return autoExportId;
    }

    @Override
    public void setImportexportId(int autoExportId) {
        this.autoExportId = autoExportId;
    }

    @Override
    public List<WorkflowDependency> getDependencies() {
        List<WorkflowDependency> dependencies = super.getDependencies();

        if (isFilled() && autoExportId > 0) {
            dependencies.add(WorkflowDependencyType.AUTO_EXPORT.forId(autoExportId));
        }

        return dependencies;
    }

    @Override
    public boolean equalsIgnoreI18n(Object o) {
        WorkflowExportImpl that = (WorkflowExportImpl) o;
        return super.equalsIgnoreI18n(o)
            && autoExportId == that.autoExportId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), autoExportId);
    }
}
