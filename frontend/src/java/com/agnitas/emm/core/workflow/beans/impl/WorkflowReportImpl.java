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

import org.apache.commons.collections4.CollectionUtils;

import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowReport;

public class WorkflowReportImpl extends BaseWorkflowIcon implements WorkflowReport {
    private List<Integer> reports = new ArrayList<>();

    public WorkflowReportImpl() {
        super();
        setType(WorkflowIconType.REPORT.getId());
    }

	@Override
	public List<Integer> getReports() {
		return reports;
	}

	@Override
	public void setReports(List<Integer> reports) {
		this.reports = reports;
	}

	@Override
	public List<WorkflowDependency> getDependencies() {
		List<WorkflowDependency> dependencies = super.getDependencies();

		if (isFilled() && CollectionUtils.isNotEmpty(reports)) {
			for (int reportId : reports) {
				dependencies.add(WorkflowDependencyType.REPORT.forId(reportId));
			}
		}

		return dependencies;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		WorkflowReportImpl that = (WorkflowReportImpl) o;
		return Objects.equals(reports, that.reports);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), reports);
	}
}
