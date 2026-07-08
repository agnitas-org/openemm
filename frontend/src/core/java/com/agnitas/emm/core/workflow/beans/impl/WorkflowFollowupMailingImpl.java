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
import com.agnitas.emm.core.workflow.beans.WorkflowFollowupMailing;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;

public class WorkflowFollowupMailingImpl extends WorkflowMailingImpl implements WorkflowFollowupMailing {
    private int baseMailingId;
    private WorkflowReactionType decisionCriterion;

    public WorkflowFollowupMailingImpl() {
        super();
        setType(WorkflowIconType.FOLLOWUP_MAILING.getId());
    }

    @Override
    public int getBaseMailingId() {
        return baseMailingId;
    }

    @Override
    public void setBaseMailingId(int baseMailingId) {
        this.baseMailingId = baseMailingId;
    }

    @Override
    public WorkflowReactionType getDecisionCriterion() {
        return decisionCriterion;
    }

    @Override
    public void setDecisionCriterion(WorkflowReactionType decisionCriterion) {
        this.decisionCriterion = decisionCriterion;
    }

    @Override
    public List<WorkflowDependency> getDependencies() {
        List<WorkflowDependency> dependencies = super.getDependencies();

        if (isFilled() && baseMailingId > 0) {
            dependencies.add(WorkflowDependencyType.MAILING_REFERENCE.forId(baseMailingId));
        }

        return dependencies;
    }

    @Override
    public boolean equalsIgnoreI18n(Object o) {
        WorkflowFollowupMailingImpl that = (WorkflowFollowupMailingImpl) o;
        return super.equalsIgnoreI18n(o)
            && baseMailingId == that.baseMailingId
            && decisionCriterion == that.decisionCriterion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baseMailingId, decisionCriterion);
    }
}
