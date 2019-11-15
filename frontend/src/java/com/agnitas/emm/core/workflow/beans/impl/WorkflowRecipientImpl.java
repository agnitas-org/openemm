/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowRecipient;

public class WorkflowRecipientImpl extends BaseWorkflowIcon implements WorkflowRecipient {
	private int mailinglistId;
    private List<Integer> targets = new ArrayList<>();
    private WorkflowTargetOption targetsOption;

    public WorkflowRecipientImpl() {
        super();
        setType(WorkflowIconType.RECIPIENT.getId());
    }

    @Override
	public int getMailinglistId() {
		return mailinglistId;
	}

	@Override
	public void setMailinglistId(int mailinglistId) {
		this.mailinglistId = mailinglistId;
	}

	@Override
	public List<Integer> getTargets() {
		return targets;
	}

	@Override
	public void setTargets(List<Integer> targets) {
		this.targets = targets;
	}

	@Override
    public WorkflowTargetOption getTargetsOption() {
        return targetsOption;
    }

    @Override
    public void setTargetsOption(WorkflowTargetOption targetsOption) {
        this.targetsOption = targetsOption;
    }

	@Override
	public List<WorkflowDependency> getDependencies() {
		List<WorkflowDependency> dependencies = super.getDependencies();

		if (isFilled() && mailinglistId > 0) {
			dependencies.add(WorkflowDependencyType.MAILINGLIST.forId(mailinglistId));
			if (targets != null) {
				for (int targetId : targets) {
					if (targetId > 0) {
						dependencies.add(WorkflowDependencyType.TARGET_GROUP.forId(targetId));
					}
				}
			}
		}

		return dependencies;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		WorkflowRecipientImpl that = (WorkflowRecipientImpl) o;
		return mailinglistId == that.mailinglistId &&
				Objects.equals(targets, that.targets) &&
				targetsOption == that.targetsOption;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), mailinglistId, targets, targetsOption);
	}
}
