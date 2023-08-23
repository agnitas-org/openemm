/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.dao;

import java.util.Date;
import java.util.List;

import org.agnitas.beans.CompaniesConstraints;


import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;

public interface ComWorkflowReportScheduleDao {

    @DaoUpdateReturnValueCheck
    void scheduleWorkflowReport(int reportId, int companyId, int workflowId, Date sendTime);

    List<Integer> getAllWorkflowReportsToSend();

    List<ComBirtReport> getAllWorkflowBirtReportsToSend();

    List<Integer> getAllWorkflowBirtReportIdsToSend(int maximumNumberOfReports, CompaniesConstraints constraints);

	void markWorkflowReportsSent(List<Integer> reportIds);
}
