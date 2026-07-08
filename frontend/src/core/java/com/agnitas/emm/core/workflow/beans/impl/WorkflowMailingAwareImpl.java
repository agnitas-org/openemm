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
import com.agnitas.emm.core.workflow.beans.WorkflowMailingAware;

public class WorkflowMailingAwareImpl extends BaseWorkflowIcon implements WorkflowMailingAware {
    private int mailingId;

    @Override
    public int getMailingId() {
        return mailingId;
    }

    @Override
    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    @Override
    public List<WorkflowDependency> getDependencies() {
        List<WorkflowDependency> dependencies = super.getDependencies();

        if (isFilled() && mailingId > 0) {
            dependencies.add(WorkflowDependencyType.MAILING_DELIVERY.forId(mailingId));
        }

        return dependencies;
    }

    @Override
    public boolean equalsIgnoreI18n(Object o) {
        WorkflowMailingAwareImpl that = (WorkflowMailingAwareImpl) o;
        return super.equalsIgnoreI18n(o)
            && mailingId == that.mailingId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mailingId);
    }
}
