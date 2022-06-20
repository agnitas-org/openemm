/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowForm;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;

public class WorkflowFormImpl extends BaseWorkflowIcon implements WorkflowForm {
    private int userFormId;
    private String formType;

    public WorkflowFormImpl() {
        super();
        setType(WorkflowIconType.FORM.getId());
    }

    @Override
	public int getUserFormId() {
        return userFormId;
    }

    @Override
	public void setUserFormId(int userFormId) {
        this.userFormId = userFormId;
    }

    @Override
	public String getFormType() {
        return formType;
    }

    @Override
	public void setFormType(String formType) {
        this.formType = formType;
    }

    @Override
    public List<WorkflowDependency> getDependencies() {
        List<WorkflowDependency> dependencies = super.getDependencies();

        if (isFilled() && userFormId > 0) {
            dependencies.add(WorkflowDependencyType.USER_FORM.forId(userFormId));
        }

        return dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkflowFormImpl that = (WorkflowFormImpl) o;
        return userFormId == that.userFormId &&
                Objects.equals(formType, that.formType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userFormId, formType);
    }
}
