/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.workflow.beans.WorkflowArchive;
import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;

public class WorkflowArchiveImpl extends BaseWorkflowIcon implements WorkflowArchive {
    private int campaignId;
    private boolean archived;

    public WorkflowArchiveImpl() {
        super();
        setType(WorkflowIconType.ARCHIVE.getId());
    }

    @Override
    public int getCampaignId() {
        return campaignId;
    }

    @Override
    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    @Override
    public boolean isArchived() {
        return archived;
    }

    @Override
    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public List<WorkflowDependency> getDependencies() {
        List<WorkflowDependency> dependencies = super.getDependencies();

        if (isFilled() && campaignId > 0) {
            dependencies.add(WorkflowDependencyType.ARCHIVE.forId(campaignId));
        }

        return dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkflowArchiveImpl that = (WorkflowArchiveImpl) o;
        return campaignId == that.campaignId &&
                archived == that.archived;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), campaignId, archived);
    }
}
