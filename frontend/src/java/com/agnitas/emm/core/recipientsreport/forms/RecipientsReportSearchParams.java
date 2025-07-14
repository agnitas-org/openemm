/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.web.forms.FormSearchParams;

public class RecipientsReportSearchParams implements FormSearchParams<RecipientsReportForm> {

    private int adminId;
    private Integer datasourceId;
    private String fileName;
    private RecipientsReport.EntityType[] types;
    private DateRange reportDate = new DateRange();

    @Override
    public void storeParams(RecipientsReportForm filter) {
        this.datasourceId = filter.getDatasourceId();
        this.adminId = filter.getAdminId();
        this.fileName = filter.getFileName();
        this.types = filter.getTypes();
        this.reportDate = filter.getReportDate();
    }

    @Override
    public void restoreParams(RecipientsReportForm filter) {
        filter.setDatasourceId(datasourceId);
        filter.setAdminId(adminId);
        filter.setFileName(fileName);
        filter.setTypes(types);
        filter.setReportDate(reportDate);
    }

    @Override
    public void resetParams() {
        adminId = 0;
        datasourceId = null;
        fileName = null;
        types = null;
        reportDate = new DateRange();
    }
}
